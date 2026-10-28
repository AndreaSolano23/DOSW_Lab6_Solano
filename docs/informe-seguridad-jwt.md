# Informe de seguridad · App ToDo con Spring Security y JWT

**Escuela Colombiana de Ingeniería Julio Garavito** · Desarrollo y Operaciones de Software (DOSW)

**Integrantes:** `<Paula Andrea Solano Morales >`

El detalle de diagramas, endpoints y evidencias está en el [README](../README.md).

---

## 1. ¿Cuál es la diferencia entre autenticación y autorización? Ejemplo real de ToDo

**Autenticación** responde "¿quién eres?". **Autorización** responde "¿qué puedes hacer?".

En ToDo, la autenticación ocurre en `POST /api/v1/auth/login`: `AuthService` busca al usuario por correo y compara la contraseña con el hash BCrypt; si es correcta, `JwtService` emite un token que identifica al usuario (`sub`). La autorización ocurre después, en cada petición con ese token. Dos ejemplos:

- Ana ya está autenticada (su token es válido), pero si pide `PUT /api/v1/tasks/25` y esa tarea es de Luis, recibe `404`: está autenticada pero no autorizada sobre esa tarea.
- Un `USER` autenticado que consulta `/api/v1/admin/summary` recibe `403`: su identidad es válida, pero su rol no alcanza.

## 2. ¿Cómo se genera un JWT, qué contienen sus partes y cómo verifica Spring Boot que no fue manipulado?

Un JWT son tres partes en Base64URL separadas por puntos: **header.payload.signature**.

- **Header:** el algoritmo de firma (`HS256`).
- **Payload (claims):** en `JwtService.issueToken` se incluyen `sub` (id del usuario), `iss` (`todo-api`), `iat` (emisión), `exp` (emisión + 30 minutos), `role` y `email`.
- **Signature:** un HMAC-SHA256 calculado sobre `header.payload` con la llave `JWT_SECRET`.

Para emitirlo, `SecurityConfig` define un `JwtEncoder` (`NimbusJwtEncoder`) con esa llave. Para validarlo define un `JwtDecoder` con la **misma** llave: en cada petición recalcula el HMAC del header y el payload recibidos y lo compara con la firma del token. Si cambió un solo carácter, las firmas no coinciden y el token se rechaza con `401`. Además valida `exp` (sin margen de tolerancia), `iss` y que existan `sub`, `iat` y `exp`.

## 3. ¿Por qué decodificar el payload de un JWT no equivale a validar su autenticidad?

Porque el payload solo está **codificado** en Base64URL, no cifrado ni protegido: cualquiera puede decodificarlo, cambiarlo y volver a codificarlo. Lo que protege el token es la firma, y solo se puede calcular con la llave secreta del servidor.

Lo probamos con `TokenForgeryIntegrationTest.adminSummary_withRoleClaimEditedToAdmin_returns401`: se toma un token válido de un `USER`, se decodifica el payload, se cambia `"role":"USER"` por `"role":"ADMIN"` y se vuelve a codificar conservando la firma original. El payload dice `ADMIN`, pero la firma ya no corresponde y el servidor responde `401`.

## 4. ¿Qué sucede exactamente cuando un token vence y el usuario intenta consultar sus tareas?

1. React envía `GET /api/v1/tasks` con `Authorization: Bearer <token>`.
2. El `JwtDecoder` valida `exp` con un `JwtTimestampValidator` sin margen (`Duration.ZERO`); como `exp` ya pasó, rechaza el token.
3. Spring Security responde **`401 Unauthorized`**.
4. En `api/http.js`, `authFetch` ve el `401` y avisa a `AuthContext`, que borra el token y el usuario.
5. `App.jsx` deja de mostrar las tareas y muestra el login con el mensaje "Your session has expired. Please log in again."

Además, `AuthContext` programa un temporizador con `expiresIn` (1800 s) que cierra la sesión sola al vencer el token, sin esperar a la siguiente petición. Probado en `TokenForgeryIntegrationTest.tasks_withExpiredToken_returns401` (backend) y en `App.test.jsx` ("goes back to login when a request answers 401", frontend).

## 5. ¿Por qué no se deben almacenar las contraseñas en texto plano? ¿Cómo se verifica la contraseña durante el login?

Si la base de datos se filtra, todas las contraseñas quedarían expuestas, y como muchas personas reutilizan contraseñas, el daño pasaría a otros servicios. También las vería cualquiera con acceso a la base de datos o a un respaldo.

En el registro, `AuthService` guarda `passwordEncoder.encode(password)`: un hash **BCrypt** con sal aleatoria, deliberadamente lento. Está evidenciado en [`10-hash-en-postgres.png`](evidence/10-hash-en-postgres.png) y probado en `AuthServiceTest.register_storesPasswordAsHash_notPlainText`. En el login no se descifra nada (el hash no es reversible): `passwordEncoder.matches(contraseñaIngresada, hashGuardado)` aplica BCrypt con la misma sal y compara. Si no coincide, o si el correo no existe, se lanza la misma `InvalidCredentialsException`, para no revelar qué correos están registrados.

## 6. ¿Qué diferencia existe entre 401 Unauthorized y 403 Forbidden? Ejemplos de los endpoints

- **401:** el servidor no pudo establecer quién eres (sin token, token alterado, vencido o de otro emisor) o las credenciales del login son incorrectas.
- **403:** sabe quién eres, pero no tienes permiso para ese recurso.

Ejemplos de nuestra API:

| Petición | Código | Por qué |
|---|---|---|
| `GET /api/v1/tasks` sin `Authorization` | `401` | No hay identidad |
| `GET /api/v1/users/me` con token alterado | `401` | La firma no es válida |
| `POST /api/v1/auth/login` con contraseña incorrecta | `401` | Credenciales inválidas |
| `GET /api/v1/admin/summary` con token de un `USER` | `403` | Identidad válida, falta el rol `ADMIN` |
| `GET /api/v1/admin/summary` con token de un `ADMIN` | `200` | Identidad y rol correctos |

Evidencia: [`13`](evidence/13-sin-token-401.png), [`14`](evidence/14-token-alterado-401.png) y [`17`](evidence/17-admin-403-y-200.png).

## 7. ¿Cómo se impidió que un usuario editara una tarea de otro, incluso con un JWT válido?

La identidad nunca viene del cliente: `TaskController` toma el id del usuario del claim `sub` del token ya validado. Luego `TaskServiceImpl` busca cualquier tarea con `taskRepository.findByIdAndOwnerId(id, userId)`, es decir, la consulta **ya incluye al dueño**. Si la tarea no existe o es de otro usuario, el resultado es el mismo y se lanza `TaskNotFoundException` → `404`. La misma regla se aplica a consultar, editar y eliminar. Al crear, el dueño se asigna con el usuario autenticado y se ignora cualquier dueño enviado por el cliente.

Pruebas: `TaskOwnershipIntegrationTest` (`updateForeignTask_returns404_andDatabaseIsUnchanged`, `deleteForeignTask_returns404_andTaskStillExists`, `createTask_ignoresOwnerSentByClient`) y `TaskRepositoryOwnerTest`. Respondemos `404` y no `403` para no revelar que la tarea de otra persona existe.

## 8. ¿Por qué ocultar botones en React no es suficiente para proteger los datos?

Porque el frontend corre en el navegador del usuario, que lo controla: puede modificar el código, usar las herramientas del navegador o llamar la API directamente con `curl` o Postman usando el id de cualquier tarea. Lo que React muestra es solo comodidad. La protección real tiene que estar en el backend, que es el único que el usuario no controla.

En nuestro caso: aunque React solo pida `GET /api/v1/tasks`, el backend filtra por dueño en la capa de servicio y repositorio, y exige token válido en toda la API. Lo comprobamos intentando modificar una tarea ajena desde un cliente HTTP con el token de otro usuario (`404`).

## 9. ¿Qué ventajas y limitaciones tuvo conservar el JWT en memoria en el frontend?

**Ventajas:** el token no queda guardado en `localStorage` ni `sessionStorage`, por lo que un script malicioso (XSS) no puede leerlo de ahí ni sobrevive a cerrar la pestaña; no se manda automáticamente en cada petición como una cookie, lo que evita CSRF; y la sesión se limpia con solo borrar una variable (`api/session.js`).

**Limitaciones:** al recargar la página (F5) se pierde el token y hay que iniciar sesión otra vez; la sesión no se comparte entre pestañas; y no elimina el riesgo de XSS por completo, porque un script inyectado aún podría usar la sesión mientras la página está abierta. Aceptamos la pérdida de sesión al recargar porque el enunciado lo definió así y evita persistir el JWT.

## 10. ¿Qué limitación tiene el cierre de sesión sin revocación de tokens? ¿Qué solución propondría para un sistema real?

"Log out" solo borra el token **del lado del cliente**. El servidor es stateless y no guarda qué tokens existen, así que un token ya emitido sigue siendo válido hasta su `exp` (máximo 30 minutos). Si alguien copió el token antes del cierre de sesión, puede seguir usándolo hasta que venza.

Para un sistema real propondría:

- Tokens de acceso de **vida muy corta** (por ejemplo 5 a 15 minutos) más un *refresh token* guardado en una cookie `HttpOnly` y rotado en cada uso.
- Una **lista de revocación** (denylist) con el identificador `jti` de cada token cerrado, guardada en Redis o la base de datos con expiración igual a la del token.
- Revocar los refresh tokens del usuario al cerrar sesión, cambiar contraseña o detectar actividad sospechosa.

## 11. ¿Cuáles son las consecuencias de almacenar una llave de firma JWT dentro de GitHub?

La llave es lo único que impide falsificar tokens. Si se publica, cualquiera que la lea puede emitir un JWT válido con el `sub` y el `role` que quiera, por ejemplo `ADMIN`, y el backend lo aceptaría porque la firma sería correcta. Eso rompe autenticación y autorización por completo. Además, el historial de Git conserva la llave aunque se borre después; habría que **rotarla** (lo que invalida todos los tokens existentes), y existen bots que escanean repositorios públicos buscando justamente este tipo de secretos.

Cómo lo evitamos: la llave sale de la variable de entorno `JWT_SECRET` (`application.properties` solo contiene `${JWT_SECRET}`), `.env` está en `.gitignore`, `.env.example` no trae valores, y el backend **no arranca** si falta o tiene menos de 32 caracteres (`JwtProperties`). Las pruebas usan su propia llave de prueba en `src/test/resources`.

## 12. ¿Qué pruebas del equipo demuestran la seguridad del CRUD y no solamente su funcionamiento correcto?

Las que intentan **romper** el sistema y verifican que falla de la forma correcta:

- **Sin identidad:** `AuthJwtIntegrationTest.tasks_withoutToken_returns401`, `SecurityConfigTest` y `TokenForgeryIntegrationTest.updateTask_withoutToken_returns401`.
- **Token falsificado o vencido:** `tasks_withTamperedToken_returns401`, `tasks_withExpiredToken_returns401`, `me_withTokenFromAnotherIssuer_returns401` y `JwtServiceTest.decode_withTokenSignedWithAnotherKey_isRejected`.
- **Escalada de privilegios:** `adminSummary_withRoleClaimEditedToAdmin_returns401`, `adminSummary_withAdminTokenSignedWithAnotherKey_returns401`, `AdminAccessIntegrationTest.summary_withUserRole_returns403` y `publicRegistration_cannotCreateAnAdmin`.
- **Acceso cruzado entre usuarios:** `TaskOwnershipIntegrationTest` (consultar, modificar y eliminar tarea ajena dan `404` y **la base de datos no cambia**) y `TaskRepositoryOwnerTest`.
- **Datos inválidos:** `TaskValidationIntegrationTest` (`400` y la tarea queda intacta).
- **Frontend:** `App.test.jsx` (cierre de sesión, `401` que redirige al login y que otro usuario no ve las tareas del anterior).

Una prueba de "funcionamiento correcto" solo confirma que el camino feliz sirve; estas confirman que el camino malicioso es rechazado. Por eso el 97 % de cobertura de JaCoCo no es el argumento: lo es que los intentos de ataque listados anteriormente fallan con el código esperado.

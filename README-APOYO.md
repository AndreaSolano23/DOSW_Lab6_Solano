# README de apoyo · Parcial DOSW (segundo tercio)

Guía de comandos y plantillas para trabajar sobre la App ToDo.
Los comandos de Docker  `todo-postgres`;  se verifica con `docker ps`.

## 0. Datos del proyecto

| Qué | Valor |
|---|---|
| Backend | Spring Boot, puerto `8080`, prefijo `/api/v1` |
| Frontend | React + Vite, `http://localhost:5173` |
| Base de datos | PostgreSQL 17 en Docker, puerto `5432` |
| Contenedor | `todo-postgres` |
| Volumen | `todo-postgres-data` |
| BD / usuario / clave | `todo_db` / `todo_user` / `todo_password` |
| Esquema | `database/001_create_schema.sql` (JPA usa `ddl-auto=validate`: **no crea tablas**) |

---

## 1. Levantar el proyecto

### 1.1 PostgreSQL (Docker)

```bash
docker ps                      # contenedores corriendo
docker ps -a                   # todos (también los detenidos)
docker start todo-postgres     # arrancar el contenedor existente
docker stop todo-postgres      # detenerlo
docker logs todo-postgres      # ver errores
```

Si el contenedor no existe, se crea una sola vez:

```bash
docker pull postgres:17-alpine
docker volume create todo-postgres-data
docker run -d --name todo-postgres -e POSTGRES_DB=todo_db -e POSTGRES_USER=todo_user -e POSTGRES_PASSWORD=todo_password -p 5432:5432 -v todo-postgres-data:/var/lib/postgresql/data postgres:17-alpine
```

**Aplicar un script SQL** (cambiar el nombre del archivo en cada script nuevo):

```bash
docker cp database/001_create_schema.sql todo-postgres:/001_create_schema.sql
docker exec -i todo-postgres psql -U todo_user -d todo_db -f /001_create_schema.sql
```

**Entrar a la base y consultar:**

```bash
docker exec -it todo-postgres psql -U todo_user -d todo_db
```

Dentro de `psql`:

```text
\dt                    -- listar tablas
\d tasks               -- ver columnas de una tabla
SELECT * FROM tasks;   -- consultar datos
\q                     -- salir
```

Consulta rápida sin entrar:

```bash
docker exec -it todo-postgres psql -U todo_user -d todo_db -c "SELECT * FROM tasks;"
```

### 1.2 Backend (Spring Boot + Maven)

```bash
cd backend
./mvnw spring-boot:run          # Linux / Mac / Git Bash
.\mvnw.cmd spring-boot:run      # PowerShell / CMD
```

Queda en `http://localhost:8080`. Causas comunes cuando no arranca: Docker apagado,
script SQL sin aplicar, o entidad que no coincide con la tabla.

### 1.3 Frontend (React + Vite)

```bash
cd frontend
npm install        # solo la primera vez
npm run dev
```

Se abre en `http://localhost:5173`.

### 1.4 Pruebas

```bash
# Backend (desde /backend)
./mvnw test                                    # todas
./mvnw test -Dtest=NombreDeLaClaseTest         # una clase
./mvnw verify                                  # compila + pruebas

# Reporte JaCoCo (se genera con ./mvnw test)
# backend/target/site/jacoco/index.html

# Frontend (desde /frontend)
npm test
```

---

## 2. Git: comandos básicos

### Clonar

```bash
git clone <URL-DEL-REPO>                  # clona en una carpeta con el nombre del repo
git clone <URL-DEL-REPO> mi-carpeta       # clona con otro nombre de carpeta
```

### Copiar un proyecto a OTRO repo (conservando historial)

1. Crear el repo nuevo vacío en GitHub (privado, sin README).
2. Ejecutar:

```bash
git clone <URL-ORIGINAL> mi-carpeta
cd mi-carpeta
git remote set-url origin <URL-REPO-NUEVO>
git remote -v                             # debe mostrar SOLO la URL nueva
git push -u origin --all
```

```

### Guardar y subir cambios

| Acción | Comando |
|---|---|
| Ver qué cambió | `git status` |
| Agregar todo | `git add .` |
| Agregar un archivo | `git add ruta/archivo` |
| Hacer commit | `git commit -m "feat: add category entity"` |
| Subir | `git push` |
| Traer cambios del remoto | `git pull` |
| Ver a dónde apunta el repo | `git remote -v` |

Antes de subir, revisar con `git status` que no aparezcan `target/` ni `node_modules/`.

**Prefijos de commit** (mensajes en inglés, como en los labs):
`feat:` funcionalidad nueva · `fix:` corrección · `docs:` documentación ·
`test:` pruebas · `refactor:` mejora sin cambiar comportamiento · `chore:` otros.

### Ramas

| Acción | Comando |
|---|---|
| Ver ramas locales | `git branch` |
| Ver todas (con remotas) | `git branch -a` |
| Crear una rama y cambiarse a ella | `git checkout -b feature/nombre` |
| Cambiarse a una rama | `git checkout develop` |
| Subir una rama nueva | `git push -u origin feature/nombre` |
| Bajar una rama remota | `git fetch` y luego `git checkout nombre-de-la-rama` |
| Borrar rama local | `git branch -d feature/nombre` |

### Unir una rama a `develop`

**Opción A: Pull Request en GitHub** (la usada en el lab)

```bash
git push -u origin feature/nombre         # sube la rama
```

En GitHub: botón **Compare & pull request** → base `develop` → **Create pull request**
→ **Merge pull request**. Después, en local:

```bash
git checkout develop
git pull
```

**Opción B: merge local**

```bash
git checkout develop
git pull
git merge feature/nombre
git push
```

### Ver historial (captura de la entrega inicial)

```bash
git log --oneline                                          # corto
git log --oneline --graph --all                            # con ramas
git log --pretty=format:"%h %an %ad %s" --date=format:"%Y-%m-%d %H:%M"   # con fecha y hora
```

El último commit de la versión inicial debe tener hora anterior a las 10:00 a. m.

### Deshacer cosas

| Situación | Comando |
|---|---|
| Descartar cambios de un archivo (sin commit) | `git restore ruta/archivo` |
| Sacar un archivo del `git add` | `git restore --staged ruta/archivo` |
| Deshacer el último commit (conserva los cambios) | `git reset --soft HEAD~1` |
| Guardar cambios temporalmente | `git stash` |
| Recuperarlos | `git stash pop` |
| `push` rechazado | `git pull` y luego `git push` |


---

## 3. Flujo de trabajo sugerido

```bash
git checkout develop
git pull
git checkout -b feature/docs-historias       # 1. historias de usuario
# ... trabajar, luego:
git add . && git commit -m "docs: add user stories" && git push -u origin feature/docs-historias

git checkout develop && git merge feature/docs-historias   # (o PR en GitHub)
git checkout -b feature/nombre-backend       # 2. base de datos + backend
git checkout -b feature/nombre-frontend      # 3. React
git checkout -b feature/nombre-tests         # 4. pruebas
```

Un commit por cada paso importante: el historial también se evalúa.

---

## 4. Probar la API (curl)

En PowerShell se usa `curl.exe` en lugar de `curl`.

```bash
curl -i http://localhost:8080/api/v1/tasks                       # listar
curl -i http://localhost:8080/api/v1/tasks/1                     # uno
curl -i -X POST http://localhost:8080/api/v1/tasks -H "Content-Type: application/json" -d "{\"title\":\"Prueba\"}"
curl -i -X PUT http://localhost:8080/api/v1/tasks/1 -H "Content-Type: application/json" -d "{\"title\":\"Editada\",\"status\":\"PENDING\",\"priority\":\"HIGH\"}"
curl -i -X DELETE http://localhost:8080/api/v1/tasks/1
```

**Códigos HTTP:** `200` consultar/editar · `201` crear · `204` eliminar ·
`400` datos inválidos · `404` no existe · `409` conflicto (por ejemplo, nombre repetido).

---


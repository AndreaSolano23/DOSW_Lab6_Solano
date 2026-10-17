# # ToDo Full Stack

Aplicación de gestión de tareas con React + Spring Boot + PostgreSQL.

Informe completo, evidencias y video de demostración: [`docs/informe-laboratorio.md`](docs/informe-laboratorio.md)

# SECCION PARCIAL TERCIO 2 
HU-01 Crear categoría (RF-01)

Como usuario, quiero crear una categoría con nombre y color, para agrupar mis tareas.

Dado un nombre de 3 a 40 caracteres y un color de la paleta, cuando guardo, entonces se crea (201) y aparece en la lista.

Dado un nombre repetido (sin importar mayúsculas ni espacios), cuando guardo, entonces recibo 409 y un mensaje claro.

Dado un nombre vacío o fuera de rango, cuando guardo, entonces recibo 400.
HU-02 Consultar categorías (RF-02)

Como usuario, quiero ver mis categorías con nombre y color.

Cuando abro la app, entonces se listan desde la API, con nombre y color.
HU-03 Editar categoría (RF-03)

Como usuario, quiero cambiar el nombre o el color de una categoría.

Dado un cambio válido, cuando guardo, entonces se actualiza y las tareas muestran el nuevo nombre y color.

Dado un nombre ya usado por otra categoría, entonces recibo 409.

Dado un id inexistente, entonces recibo 404.
HU-04 Eliminar categoría (RF-04)

Como usuario, quiero eliminar una categoría que no uso.

Dada una categoría sin tareas, cuando la elimino, entonces recibo 204 y desaparece.

Dada una categoría con tareas, cuando la elimino, entonces recibo 409, no se borra y el front muestra un mensaje comprensible.
HU-05 Asignar categoría a una tarea (RF-05)

Como usuario, quiero asignar, cambiar o quitar la categoría al crear o editar una tarea.

Dado el selector con la opción "Sin categoría", cuando guardo, entonces la tarea queda con esa categoría o sin ella.

Dado un categoryId inexistente, entonces recibo 404.
HU-06 Filtrar tareas (RF-06)

Como usuario, quiero filtrar el listado por categoría.

Cuando elijo "Todas", entonces veo todas las tareas.

Cuando elijo una categoría, entonces la API devuelve solo las tareas de esa categoría.

Cuando elijo "Sin categoría", entonces veo solo las tareas sin categoría.

Si no hay resultados, entonces veo un mensaje de lista vacía.
HU-07 Conservar el CRUD existente (RF-07)

Como usuario, quiero que mis tareas anteriores sigan funcionando.

Dada la migración aplicada, entonces las tareas existentes siguen ahí, sin categoría.

Cuando creo, edito, cambio el estado o elimino una tarea, entonces funciona igual que antes.
 
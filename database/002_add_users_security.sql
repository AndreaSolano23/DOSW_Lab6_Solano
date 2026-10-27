BEGIN;

-- 1. Tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_lower CHECK (email = lower(email)),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

-- 2. Columna del dueño, nullable por ahora para no perder las tareas anteriores
ALTER TABLE tasks ADD COLUMN user_id BIGINT;

-- 3. Usuario "legacy" dueño de las tareas anteriores.
--    El hash '!' no es un hash BCrypt valido, asi que nadie puede iniciar sesion con esta cuenta.
INSERT INTO users (name, email, password_hash, role)
VALUES ('Legacy Owner', 'legacy@todo.local', '!', 'USER');

-- 4. Asignar las tareas existentes al usuario legacy
UPDATE tasks
SET user_id = (SELECT id FROM users WHERE email = 'legacy@todo.local')
WHERE user_id IS NULL;

-- 5. Ahora si: obligatorio + llave foranea + indice
ALTER TABLE tasks ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_tasks_user_id ON tasks (user_id);

COMMIT;

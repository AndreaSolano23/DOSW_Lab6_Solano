CREATE TABLE tasks (

    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(120) NOT NULL,

    description VARCHAR(500),

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',

    due_date DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_task_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),

    CONSTRAINT chk_task_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);
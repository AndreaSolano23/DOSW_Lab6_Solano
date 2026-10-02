package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import java.time.LocalDate;

public record TaskUpdateRequest(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
) {}
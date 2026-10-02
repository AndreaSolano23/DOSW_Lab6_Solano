package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import java.time.LocalDate;

public record TaskCreateRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate
) {}
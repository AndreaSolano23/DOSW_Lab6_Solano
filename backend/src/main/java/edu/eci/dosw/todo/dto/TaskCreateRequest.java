package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TaskCreateRequest(
        @NotBlank(message = "Title is required") String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate
) {}

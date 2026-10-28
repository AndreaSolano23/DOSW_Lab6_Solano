package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must have at most 120 characters") String title,
        @Size(max = 500, message = "Description must have at most 500 characters") String description,
        TaskPriority priority,
        LocalDate dueDate
) {}

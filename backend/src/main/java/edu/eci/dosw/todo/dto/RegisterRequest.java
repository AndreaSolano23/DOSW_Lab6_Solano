package edu.eci.dosw.todo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must have at most 100 characters") String name,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") @Size(max = 150, message = "Email must have at most 150 characters") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters") String password
) {}

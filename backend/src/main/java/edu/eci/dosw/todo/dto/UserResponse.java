package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.Role;

// Nunca incluye la contraseña ni su hash
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {}

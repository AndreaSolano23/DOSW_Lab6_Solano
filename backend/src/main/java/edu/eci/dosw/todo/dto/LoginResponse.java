package edu.eci.dosw.todo.dto;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresIn   // segundos
) {}

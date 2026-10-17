package edu.eci.dosw.todo.dto;
import jakarta.validation.constraints.NotBlank;

public record categoriaCreateRequest (
    @NotBlank(message = "se requiere un nombre") String nombre,
    String Color
) {}

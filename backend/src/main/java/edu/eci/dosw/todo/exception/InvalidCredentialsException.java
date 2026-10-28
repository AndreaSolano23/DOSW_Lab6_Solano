package edu.eci.dosw.todo.exception;

public class InvalidCredentialsException extends RuntimeException {
    // Mismo mensaje para correo inexistente y contraseña incorrecta, para no revelar cuál falló
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}

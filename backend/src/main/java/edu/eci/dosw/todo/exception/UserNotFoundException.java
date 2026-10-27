package edu.eci.dosw.todo.exception;

// Token válido de un usuario que ya no existe
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User no longer exists");
    }
}

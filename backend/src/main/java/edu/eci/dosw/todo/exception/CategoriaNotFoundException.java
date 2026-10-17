package edu.eci.dosw.todo.exception;

public class CategoriaNotFoundException  extends RuntimeException{
    public CategoriaNotFoundException(Long id) {
        super("Categoria no encontrada con id: " + id);
    }
    
}

package edu.eci.dosw.todo.service;
import edu.eci.dosw.todo.dto.categoriaCreateRequest;
import edu.eci.dosw.todo.dto.CategoriaResponse; 
import java.util.List;

public interface categoriaService {
    List<CategoriaResponse> findAll();

    CategoriaResponse findById(Long id);

    CategoriaResponse create(categoriaCreateRequest request);

    void delete(Long id);
}


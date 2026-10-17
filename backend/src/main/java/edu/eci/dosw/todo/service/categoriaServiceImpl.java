package edu.eci.dosw.todo.service;
import edu.eci.dosw.todo.dto.categoriaCreateRequest;
import edu.eci.dosw.todo.dto.CategoriaResponse;
import edu.eci.dosw.todo.entity.categoriasEntity; 
import edu.eci.dosw.todo.exception.CategoriaNotFoundException;
import edu.eci.dosw.todo.repository.categoriasRepository; 
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class categoriaServiceImpl implements categoriaService {
    private final categoriasRepository categoriasRepository;

    // inyección por constructor
    public categoriaServiceImpl(categoriasRepository categoriasRepository) {
        this.categoriasRepository = categoriasRepository;
    }
    
    @Override
    public List<CategoriaResponse> findAll() {
        return categoriasRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoriaResponse findById(Long id) {
        categoriasEntity nombre = findEntityById(id);
        return toResponse(nombre);
    }

}


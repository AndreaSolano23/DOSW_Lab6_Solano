package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import java.util.List;

// Todas las operaciones reciben el id del usuario autenticado (sale del JWT, nunca del cliente)
public interface TaskService {

    List<TaskResponse> findAll(Long userId);

    TaskResponse findById(Long userId, Long id);

    TaskResponse create(Long userId, TaskCreateRequest request);

    TaskResponse update(Long userId, Long id, TaskUpdateRequest request);

    void delete(Long userId, Long id);
}

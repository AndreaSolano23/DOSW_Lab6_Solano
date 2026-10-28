package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // inyección por constructor
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TaskResponse> findAll(Long userId) {
        return taskRepository.findByOwnerId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TaskResponse findById(Long userId, Long id) {
        return toResponse(findOwnedTask(userId, id));
    }

    @Override
    public TaskResponse create(Long userId, TaskCreateRequest request) {
        TaskEntity task = new TaskEntity();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        task.setStatus(TaskStatus.PENDING);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setCreatedAt(LocalDateTime.now());
        // El dueño es el usuario autenticado, no un dato que envía el cliente
        task.setOwner(userRepository.getReferenceById(userId));

        TaskEntity saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Override
    public TaskResponse update(Long userId, Long id, TaskUpdateRequest request) {
        TaskEntity task = findOwnedTask(userId, id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        TaskEntity updated = taskRepository.save(task);
        return toResponse(updated);
    }

    @Override
    public void delete(Long userId, Long id) {
        TaskEntity task = findOwnedTask(userId, id);
        taskRepository.delete(task);
    }

    // Si la tarea no existe O es de otro usuario, la respuesta es la misma (404):
    // así no se revela que el recurso de otra persona existe.
    private TaskEntity findOwnedTask(Long userId, Long id) {
        return taskRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(TaskEntity task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }
}

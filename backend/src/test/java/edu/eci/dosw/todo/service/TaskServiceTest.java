package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskEntity existingTask;

    @BeforeEach
    void setUp() {
        existingTask = new TaskEntity();
        existingTask.setId(1L);
        existingTask.setTitle("Estudiar para el parcial");
        existingTask.setStatus(TaskStatus.PENDING);
        existingTask.setPriority(TaskPriority.MEDIUM);
    }

    @Test
    void findById_deberiaRetornarTarea_cuandoExiste() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskResponse response = taskService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Estudiar para el parcial");
    }

    @Test
    void findById_deberiaLanzarExcepcion_cuandoNoExiste() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void create_deberiaAsignarValoresPorDefecto() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nueva tarea", "Descripción", null, LocalDate.now()
        );
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TaskResponse response = taskService.create(request);

        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void findAll_deberiaRetornarListaDeTareas() {
        when(taskRepository.findAll()).thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void delete_deberiaEliminarTarea_cuandoExiste() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        taskService.delete(1L);

        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    void delete_deberiaLanzarExcepcion_cuandoNoExiste() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).delete(any());
    }
}
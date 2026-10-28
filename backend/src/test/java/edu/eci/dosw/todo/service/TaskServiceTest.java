package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

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
    void findById_deberiaRetornarTarea_cuandoEsDelUsuario() {
        when(taskRepository.findByIdAndOwnerId(1L, OWNER_ID)).thenReturn(Optional.of(existingTask));

        TaskResponse response = taskService.findById(OWNER_ID, 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Estudiar para el parcial");
    }

    // PT-10: tarea inexistente o de otro usuario => misma excepción (404)
    @Test
    void findById_deberiaLanzarExcepcion_cuandoNoExisteONoEsDelUsuario() {
        when(taskRepository.findByIdAndOwnerId(1L, OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(OTHER_USER_ID, 1L))
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

        TaskResponse response = taskService.create(OWNER_ID, request);

        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void create_deberiaRespetarPrioridadEnviada() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tarea urgente", "Descripción", TaskPriority.HIGH, LocalDate.now()
        );
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        TaskResponse response = taskService.create(OWNER_ID, request);

        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    // PT-13: el dueño es el usuario autenticado
    @Test
    void create_deberiaAsignarComoDuenoAlUsuarioAutenticado() {
        UserEntity owner = new UserEntity();
        owner.setId(OWNER_ID);
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner);
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.create(OWNER_ID, new TaskCreateRequest("Mi tarea", null, null, null));

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isSameAs(owner);
    }

    @Test
    void findAll_deberiaRetornarSoloLasTareasDelUsuario() {
        when(taskRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(existingTask));

        List<TaskResponse> result = taskService.findAll(OWNER_ID);

        assertThat(result).hasSize(1);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void update_deberiaActualizarTarea_cuandoEsDelUsuario() {
        when(taskRepository.findByIdAndOwnerId(1L, OWNER_ID)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(OWNER_ID, 1L, new TaskUpdateRequest(
                "Título nuevo", "desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.now()));

        assertThat(response.title()).isEqualTo("Título nuevo");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    // PT-11: modificar una tarea ajena no cambia nada
    @Test
    void update_deberiaLanzarExcepcionYNoGuardar_cuandoLaTareaEsAjena() {
        when(taskRepository.findByIdAndOwnerId(1L, OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(OTHER_USER_ID, 1L, new TaskUpdateRequest(
                "Hackeada", "x", TaskStatus.COMPLETED, TaskPriority.LOW, null)))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_deberiaEliminarTarea_cuandoEsDelUsuario() {
        when(taskRepository.findByIdAndOwnerId(1L, OWNER_ID)).thenReturn(Optional.of(existingTask));

        taskService.delete(OWNER_ID, 1L);

        verify(taskRepository, times(1)).delete(existingTask);
    }

    // PT-12: eliminar una tarea ajena no borra nada
    @Test
    void delete_deberiaLanzarExcepcionYNoBorrar_cuandoNoExisteOEsAjena() {
        when(taskRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(OWNER_ID, 99L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).delete(any());
    }
}

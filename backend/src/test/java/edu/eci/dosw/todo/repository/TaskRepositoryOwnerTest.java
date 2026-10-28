package edu.eci.dosw.todo.repository;

import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Usa una base de datos H2 en memoria; el esquema lo crea Hibernate solo para estas pruebas
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class TaskRepositoryOwnerTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity ana;
    private UserEntity luis;
    private TaskEntity anaTask;
    private TaskEntity luisTask;

    @BeforeEach
    void setUp() {
        ana = userRepository.save(newUser("Ana Pérez", "ana@example.com"));
        luis = userRepository.save(newUser("Luis Gómez", "luis@example.com"));

        anaTask = taskRepository.save(newTask("Tarea de Ana", ana));
        taskRepository.save(newTask("Otra tarea de Ana", ana));
        luisTask = taskRepository.save(newTask("Tarea de Luis", luis));
    }

    @Test
    void findByOwnerId_returnsOnlyTasksOfThatUser() {
        List<TaskEntity> anaTasks = taskRepository.findByOwnerId(ana.getId());

        assertThat(anaTasks).hasSize(2);
        assertThat(anaTasks)
                .extracting(TaskEntity::getTitle)
                .containsExactlyInAnyOrder("Tarea de Ana", "Otra tarea de Ana");
    }

    @Test
    void findByOwnerId_doesNotReturnTasksOfOtherUsers() {
        List<TaskEntity> luisTasks = taskRepository.findByOwnerId(luis.getId());

        assertThat(luisTasks).hasSize(1);
        assertThat(luisTasks.get(0).getTitle()).isEqualTo("Tarea de Luis");
    }

    @Test
    void findByIdAndOwnerId_withOwnTask_returnsTask() {
        Optional<TaskEntity> result = taskRepository.findByIdAndOwnerId(anaTask.getId(), ana.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Tarea de Ana");
    }

    @Test
    void findByIdAndOwnerId_withForeignTask_returnsEmpty() {
        // Ana intenta acceder a la tarea de Luis
        Optional<TaskEntity> result = taskRepository.findByIdAndOwnerId(luisTask.getId(), ana.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndOwnerId_withUnknownId_returnsEmpty() {
        Optional<TaskEntity> result = taskRepository.findByIdAndOwnerId(99999L, ana.getId());

        assertThat(result).isEmpty();
    }

    private UserEntity newUser(String name, String email) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("hash-de-prueba");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private TaskEntity newTask(String title, UserEntity owner) {
        TaskEntity task = new TaskEntity();
        task.setTitle(title);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setCreatedAt(LocalDateTime.now());
        task.setOwner(owner);
        return task;
    }
}

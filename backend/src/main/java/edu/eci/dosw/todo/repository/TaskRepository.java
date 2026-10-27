package edu.eci.dosw.todo.repository;

import edu.eci.dosw.todo.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    // Solo las tareas del usuario dueño
    List<TaskEntity> findByOwnerId(Long ownerId);

    // Una tarea por id, solo si pertenece a ese usuario
    Optional<TaskEntity> findByIdAndOwnerId(Long id, Long ownerId);
}

package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import java.util.List;

public interface TaskService {

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse create(TaskCreateRequest request);

    TaskResponse update(Long id, TaskUpdateRequest request);

    void delete(Long id);
}
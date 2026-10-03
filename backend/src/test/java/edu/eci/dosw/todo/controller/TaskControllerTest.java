package edu.eci.dosw.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskResponse sampleResponse() {
        return new TaskResponse(
                1L, "Estudiar para el parcial", "Descripción",
                TaskStatus.PENDING, TaskPriority.MEDIUM,
                LocalDate.now(), LocalDateTime.now()
        );
    }

    @Test
    void findAll_deberiaRetornar200() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_deberiaRetornar200_cuandoExiste() throws Exception {
        when(taskService.findById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Estudiar para el parcial"));
    }

    @Test
    void findById_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(taskService.findById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_deberiaRetornar201_cuandoEsValido() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nueva tarea", "Descripción", TaskPriority.HIGH, LocalDate.now()
        );
        when(taskService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_deberiaRetornar400_cuandoEsInvalido() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "", null, null, null   // title en blanco: inválido
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_deberiaRetornar200_cuandoExiste() throws Exception {
        when(taskService.update(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Actualizada","description":"desc",
                                 "status":"IN_PROGRESS","priority":"HIGH","dueDate":"2026-09-25"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void update_deberiaRetornar404_cuandoNoExiste() throws Exception {
        when(taskService.update(eq(99L), any())).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(put("/api/v1/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"X","description":"Y",
                                 "status":"PENDING","priority":"LOW","dueDate":"2026-09-25"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_deberiaRetornar204_cuandoExiste() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
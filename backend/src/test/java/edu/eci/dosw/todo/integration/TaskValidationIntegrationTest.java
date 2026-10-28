package edu.eci.dosw.todo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Datos inválidos en tareas: deben dar 400 (no 500) y no cambiar nada en la base de datos
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authit;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TaskValidationIntegrationTest extends ApiTestSupport {

    private String bearer;
    private long taskId;

    @BeforeEach
    void setUp() throws Exception {
        String email = newEmail("validacion");
        register(email);
        bearer = loginBearer(email);
        taskId = createTask(bearer, "Titulo original").get("id").asLong();
    }

    @Test
    void updateTask_withBlankTitle_returns400_andTaskIsUnchanged() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\",\"status\":\"PENDING\",\"priority\":\"LOW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required"));

        mockMvc.perform(get("/api/v1/tasks/" + taskId).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Titulo original"));
    }

    @Test
    void updateTask_withoutStatusAndPriority_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nuevo titulo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").value("Status is required"))
                .andExpect(jsonPath("$.errors.priority").value("Priority is required"));
    }

    @Test
    void updateTask_withTooLongTitle_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "a".repeat(121) + "\",\"status\":\"PENDING\",\"priority\":\"LOW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTask_withTooLongTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "a".repeat(121) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }
}

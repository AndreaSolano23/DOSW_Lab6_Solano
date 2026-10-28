package edu.eci.dosw.todo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Ana y Luis: cada uno solo puede ver y modificar sus propias tareas (HTTP real + base H2 en memoria)
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authit;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TaskOwnershipIntegrationTest extends ApiTestSupport {

    @Autowired
    private TaskRepository taskRepository;

    private long anaId;
    private long luisId;
    private String anaBearer;
    private String luisBearer;
    private long anaTaskId;
    private long luisTaskId;

    @BeforeEach
    void setUp() throws Exception {
        String anaEmail = newEmail("ana");
        String luisEmail = newEmail("luis");
        anaId = register(anaEmail);
        luisId = register(luisEmail);
        anaBearer = loginBearer(anaEmail);
        luisBearer = loginBearer(luisEmail);

        anaTaskId = createTask(anaBearer, "Tarea 1 de Ana").get("id").asLong();
        createTask(anaBearer, "Tarea 2 de Ana");
        luisTaskId = createTask(luisBearer, "Tarea de Luis").get("id").asLong();
    }

    // PT-07 / PT-18
    @Test
    void list_returnsOnlyTheTasksOfEachUser() throws Exception {
        assertThat(titlesOf(anaBearer)).containsExactlyInAnyOrder("Tarea 1 de Ana", "Tarea 2 de Ana");
        assertThat(titlesOf(luisBearer)).containsExactly("Tarea de Luis");
    }

    // PT-10
    @Test
    void getForeignTask_returns404_withoutLeakingData() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + anaTaskId).header("Authorization", luisBearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").doesNotExist());
    }

    @Test
    void getOwnTask_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + anaTaskId).header("Authorization", anaBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea 1 de Ana"));
    }

    // PT-11
    @Test
    void updateForeignTask_returns404_andDatabaseIsUnchanged() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/" + anaTaskId)
                        .header("Authorization", luisBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hackeada por Luis\",\"description\":\"x\","
                                + "\"status\":\"COMPLETED\",\"priority\":\"LOW\",\"dueDate\":\"2026-12-01\"}"))
                .andExpect(status().isNotFound());

        TaskEntity inDatabase = taskRepository.findById(anaTaskId).orElseThrow();
        assertThat(inDatabase.getTitle()).isEqualTo("Tarea 1 de Ana");
        assertThat(inDatabase.getDescription()).isEqualTo("desc");
    }

    // PT-12
    @Test
    void deleteForeignTask_returns404_andTaskStillExists() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/" + anaTaskId).header("Authorization", luisBearer))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.existsById(anaTaskId)).isTrue();
    }

    // El CRUD propio se conserva
    @Test
    void ownTask_canBeUpdatedAndDeleted() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/" + luisTaskId)
                        .header("Authorization", luisBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Editada por Luis\",\"description\":\"d\","
                                + "\"status\":\"IN_PROGRESS\",\"priority\":\"LOW\",\"dueDate\":\"2026-12-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Editada por Luis"));

        mockMvc.perform(delete("/api/v1/tasks/" + luisTaskId).header("Authorization", luisBearer))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + luisTaskId).header("Authorization", luisBearer))
                .andExpect(status().isNotFound());
    }

    // PT-13
    @Test
    void createTask_assignsTheAuthenticatedUserAsOwner() throws Exception {
        assertThat(taskRepository.findByOwnerId(anaId))
                .extracting(TaskEntity::getTitle)
                .containsExactlyInAnyOrder("Tarea 1 de Ana", "Tarea 2 de Ana");
        assertThat(taskRepository.findByOwnerId(luisId))
                .extracting(TaskEntity::getTitle)
                .containsExactly("Tarea de Luis");
    }

    // El servidor no confía en un id de usuario enviado por el cliente
    @Test
    void createTask_ignoresOwnerSentByClient() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", anaBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Intento de suplantar\",\"userId\":" + luisId + ",\"ownerId\":" + luisId + "}"))
                .andExpect(status().isCreated());

        assertThat(taskRepository.findByOwnerId(anaId))
                .extracting(TaskEntity::getTitle).contains("Intento de suplantar");
        assertThat(taskRepository.findByOwnerId(luisId))
                .extracting(TaskEntity::getTitle).doesNotContain("Intento de suplantar");
    }

    private List<String> titlesOf(String bearer) throws Exception {
        String json = mockMvc.perform(get("/api/v1/tasks").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> titles = new ArrayList<>();
        for (JsonNode task : objectMapper.readTree(json)) {
            titles.add(task.get("title").asText());
        }
        return titles;
    }
}

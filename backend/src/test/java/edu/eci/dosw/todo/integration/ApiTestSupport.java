package edu.eci.dosw.todo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Utilidades compartidas por las pruebas de integración (registro, login, creación de tareas)
abstract class ApiTestSupport {

    protected static final String PASSWORD = "ContrasenaDemo123!";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String newEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    // Registra un usuario y devuelve su id
    protected long register(String email) throws Exception {
        String json = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Usuario Prueba\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    // Inicia sesión y devuelve el header Authorization listo para usar
    protected String loginBearer(String email) throws Exception {
        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(json).get("accessToken").asText();
    }

    // Crea una tarea con el usuario del token y devuelve la respuesta JSON
    protected JsonNode createTask(String bearer, String title) throws Exception {
        String json = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"description\":\"desc\","
                                + "\"priority\":\"HIGH\",\"dueDate\":\"2026-12-01\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }
}

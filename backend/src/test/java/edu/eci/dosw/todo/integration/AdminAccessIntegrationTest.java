package edu.eci.dosw.todo.integration;

import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Autorización por roles: /api/v1/admin/summary solo para ADMIN
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authit;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AdminAccessIntegrationTest extends ApiTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void summary_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/summary"))
                .andExpect(status().isUnauthorized());
    }

    // PT-14
    @Test
    void summary_withUserRole_returns403() throws Exception {
        String email = newEmail("user");
        register(email);
        String bearer = loginBearer(email);

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", bearer))
                .andExpect(status().isForbidden());
    }

    // PT-15
    @Test
    void summary_withAdminRole_returns200WithAggregatedMetrics() throws Exception {
        String email = newEmail("admin");
        register(email);
        promoteToAdmin(email);
        String bearer = loginBearer(email);   // el rol ADMIN viaja en el token emitido en este login

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalAdmins").isNumber())
                .andExpect(jsonPath("$.totalTasks").isNumber())
                .andExpect(jsonPath("$.tasksByStatus.PENDING").isNumber())
                .andExpect(jsonPath("$.tasksByStatus.IN_PROGRESS").isNumber())
                .andExpect(jsonPath("$.tasksByStatus.COMPLETED").isNumber());
    }

    @Test
    void me_showsAdminRole_afterPromotion() throws Exception {
        String email = newEmail("admin");
        register(email);
        promoteToAdmin(email);
        String bearer = loginBearer(email);

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // Ninguna petición pública puede elegir ADMIN
    @Test
    void publicRegistration_cannotCreateAnAdmin() throws Exception {
        String email = newEmail("intruso");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Intruso\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD
                                + "\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        String bearer = loginBearer(email);
        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", bearer))
                .andExpect(status().isForbidden());
    }

    // Solo para pruebas: simula la creación local de una cuenta ADMIN
    private void promoteToAdmin(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }
}

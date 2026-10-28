package edu.eci.dosw.todo.integration;

import edu.eci.dosw.todo.config.JwtProperties;
import edu.eci.dosw.todo.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Intentos de falsificar credenciales contra los recursos protegidos del CRUD y de ADMIN
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authit;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TokenForgeryIntegrationTest extends ApiTestSupport {

    @Autowired
    private JwtEncoder jwtEncoder;

    // PT-08 sobre el CRUD: token con un carácter alterado
    @Test
    void tasks_withTamperedToken_returns401() throws Exception {
        String email = newEmail("alterado");
        register(email);
        String[] parts = loginBearer(email).substring("Bearer ".length()).split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x." + parts[2];

        mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    // PT-09 sobre el CRUD: token ya vencido
    @Test
    void tasks_withExpiredToken_returns401() throws Exception {
        long userId = register(newEmail("vencido"));
        Instant now = Instant.now();
        String expired = tokenFor(jwtEncoder, userId, "USER", now.minus(Duration.ofMinutes(10)), now.minus(Duration.ofMinutes(5)));

        mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTask_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"status\":\"PENDING\",\"priority\":\"LOW\"}"))
                .andExpect(status().isUnauthorized());
    }

    // Un token que dice ser ADMIN pero está firmado con otra llave no sirve
    @Test
    void adminSummary_withAdminTokenSignedWithAnotherKey_returns401() throws Exception {
        long userId = register(newEmail("falsificado"));
        JwtEncoder otherEncoder = new SecurityConfig().jwtEncoder(
                new JwtProperties("another-secret-key-with-more-than-32-chars!!", "todo-api", 30));
        Instant now = Instant.now();
        String forged = tokenFor(otherEncoder, userId, "ADMIN", now, now.plus(Duration.ofMinutes(30)));

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    // Escalada de privilegios: cambiar "USER" por "ADMIN" en el payload sin poder firmar de nuevo
    @Test
    void adminSummary_withRoleClaimEditedToAdmin_returns401() throws Exception {
        String email = newEmail("escalada");
        register(email);
        String[] parts = loginBearer(email).substring("Bearer ".length()).split("\\.");

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String editedPayload = payload.replaceAll("\"role\"\\s*:\\s*\"USER\"", "\"role\":\"ADMIN\"");
        assertThat(editedPayload).isNotEqualTo(payload);   // confirma que sí se modificó el rol

        String forged = parts[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(editedPayload.getBytes(StandardCharsets.UTF_8))
                + "." + parts[2];

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    private String tokenFor(JwtEncoder encoder, long userId, String role, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("todo-api")
                .subject(String.valueOf(userId))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", role)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}

package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.config.JwtProperties;
import edu.eci.dosw.todo.config.SecurityConfig;
import edu.eci.dosw.todo.dto.LoginResponse;
import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Pruebas unitarias del emisor y del validador de JWT (sin levantar Spring)
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-with-more-than-32-chars";

    private final JwtProperties props = new JwtProperties(SECRET, "todo-api", 30);
    private final SecurityConfig config = new SecurityConfig();
    private final JwtEncoder encoder = config.jwtEncoder(props);
    private final JwtDecoder decoder = config.jwtDecoder(props);
    private final JwtService jwtService = new JwtService(encoder, props);

    // PT-04
    @Test
    void issueToken_returnsBearerTokenThatLasts30Minutes() {
        LoginResponse response = jwtService.issueToken(user(7L, Role.USER));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(1800);
        assertThat(response.accessToken().split("\\.")).hasSize(3);
    }

    @Test
    void issueToken_containsExpectedClaims() {
        String token = jwtService.issueToken(user(7L, Role.ADMIN)).accessToken();

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("todo-api");
        assertThat((String) jwt.getClaim("role")).isEqualTo("ADMIN");
        assertThat((String) jwt.getClaim("email")).isEqualTo("ana@example.com");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void issueToken_doesNotContainPassword() {
        String token = jwtService.issueToken(user(7L, Role.USER)).accessToken();

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getClaims()).doesNotContainKeys("password", "passwordHash");
    }

    // PT-08
    @Test
    void decode_withTamperedSignature_isRejected() {
        String token = jwtService.issueToken(user(7L, Role.USER)).accessToken();
        String[] parts = token.split("\\.");
        char original = parts[2].charAt(5);
        String tamperedSignature = parts[2].substring(0, 5) + (original == 'A' ? 'B' : 'A') + parts[2].substring(6);

        assertThatThrownBy(() -> decoder.decode(parts[0] + "." + parts[1] + "." + tamperedSignature))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void decode_withModifiedPayload_isRejected() {
        String token = jwtService.issueToken(user(7L, Role.USER)).accessToken();
        String[] parts = token.split("\\.");

        assertThatThrownBy(() -> decoder.decode(parts[0] + "." + parts[1] + "x." + parts[2]))
                .isInstanceOf(JwtException.class);
    }

    // PT-09
    @Test
    void decode_withExpiredToken_isRejected() {
        Instant now = Instant.now();
        String expired = encode(encoder, "todo-api", "7", now.minus(Duration.ofMinutes(10)), now.minus(Duration.ofMinutes(5)));

        assertThatThrownBy(() -> decoder.decode(expired))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void decode_withWrongIssuer_isRejected() {
        Instant now = Instant.now();
        String token = encode(encoder, "otro-emisor", "7", now, now.plus(Duration.ofMinutes(30)));

        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void decode_withTokenSignedWithAnotherKey_isRejected() {
        JwtEncoder otherEncoder = config.jwtEncoder(
                new JwtProperties("another-secret-key-with-more-than-32-chars!!", "todo-api", 30));
        Instant now = Instant.now();
        String forged = encode(otherEncoder, "todo-api", "7", now, now.plus(Duration.ofMinutes(30)));

        assertThatThrownBy(() -> decoder.decode(forged))
                .isInstanceOf(JwtException.class);
    }

    private String encode(JwtEncoder jwtEncoder, String issuer, String subject, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", "USER")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private UserEntity user(Long id, Role role) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName("Ana Pérez");
        user.setEmail("ana@example.com");
        user.setRole(role);
        return user;
    }
}

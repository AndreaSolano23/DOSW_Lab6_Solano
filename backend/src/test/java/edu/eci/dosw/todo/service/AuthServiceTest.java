package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.LoginRequest;
import edu.eci.dosw.todo.dto.RegisterRequest;
import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.exception.EmailAlreadyExistsException;
import edu.eci.dosw.todo.exception.InvalidCredentialsException;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    // PT-01
    @Test
    void register_withValidData_createsUserWithRoleUser() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = authService.register(
                new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    // PT-03
    @Test
    void register_storesPasswordAsHash_notPlainText() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!"));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        String stored = captor.getValue().getPasswordHash();

        assertThat(stored).isNotEqualTo("ContrasenaDemo123!");
        assertThat(stored).startsWith("$2");
        assertThat(passwordEncoder.matches("ContrasenaDemo123!", stored)).isTrue();
    }

    @Test
    void register_normalizesEmailAndTrimsName() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(
                new RegisterRequest("  Ana Pérez  ", "  ANA@Example.com ", "ContrasenaDemo123!"));

        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.name()).isEqualTo("Ana Pérez");
    }

    // PT-02
    @Test
    void register_withExistingEmail_throwsAndDoesNotSave() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_withCorrectCredentials_returnsUser() {
        UserEntity user = userWithPassword("ana@example.com", "ContrasenaDemo123!");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

        UserEntity result = authService.authenticate(new LoginRequest("Ana@Example.com", "ContrasenaDemo123!"));

        assertThat(result.getEmail()).isEqualTo("ana@example.com");
    }

    // PT-05
    @Test
    void authenticate_withWrongPassword_throwsInvalidCredentials() {
        UserEntity user = userWithPassword("ana@example.com", "ContrasenaDemo123!");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticate(new LoginRequest("ana@example.com", "otra-clave-1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticate_withUnknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new LoginRequest("nadie@example.com", "ContrasenaDemo123!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private UserEntity userWithPassword(String email, String rawPassword) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Pérez");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        return user;
    }
}

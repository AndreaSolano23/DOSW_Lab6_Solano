package edu.eci.dosw.todo.repository;

import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(newUser("Ana Pérez", "ana@example.com"));
    }

    @Test
    void findByEmail_withExistingEmail_returnsUser() {
        Optional<UserEntity> result = userRepository.findByEmail("ana@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Ana Pérez");
        assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void findByEmail_withUnknownEmail_returnsEmpty() {
        assertThat(userRepository.findByEmail("nadie@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrueOnlyForExistingEmail() {
        assertThat(userRepository.existsByEmail("ana@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nadie@example.com")).isFalse();
    }

    @Test
    void save_withDuplicateEmail_throwsException() {
        UserEntity duplicate = newUser("Otra Ana", "ana@example.com");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserEntity newUser(String name, String email) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("hash-de-prueba");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}

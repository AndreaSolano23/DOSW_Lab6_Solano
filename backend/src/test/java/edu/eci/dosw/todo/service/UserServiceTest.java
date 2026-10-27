package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.exception.UserNotFoundException;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfile_withExistingUser_returnsProfileWithoutSecrets() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Pérez");
        user.setEmail("ana@example.com");
        user.setPasswordHash("hash-secreto");
        user.setRole(Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse profile = userService.getProfile(1L);

        assertThat(profile.id()).isEqualTo(1L);
        assertThat(profile.email()).isEqualTo("ana@example.com");
        assertThat(profile.role()).isEqualTo(Role.USER);
    }

    @Test
    void getProfile_withUnknownUser_throwsUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(UserNotFoundException.class);
    }
}

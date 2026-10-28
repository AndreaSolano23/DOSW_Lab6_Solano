package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.AdminSummaryResponse;
import edu.eci.dosw.todo.entity.Role;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getSummary_returnsOnlyAggregatedMetrics() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        when(taskRepository.count()).thenReturn(7L);
        when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(2L);

        AdminSummaryResponse summary = adminService.getSummary();

        assertThat(summary.totalUsers()).isEqualTo(5);
        assertThat(summary.totalAdmins()).isEqualTo(1);
        assertThat(summary.totalTasks()).isEqualTo(7);
        assertThat(summary.tasksByStatus())
                .containsEntry("PENDING", 3L)
                .containsEntry("IN_PROGRESS", 2L)
                .containsEntry("COMPLETED", 2L);
    }
}

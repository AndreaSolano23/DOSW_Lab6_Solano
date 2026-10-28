package edu.eci.dosw.todo.dto;

import java.util.Map;

// Solo métricas agregadas: no expone tareas, correos ni datos privados de nadie
public record AdminSummaryResponse(
        long totalUsers,
        long totalAdmins,
        long totalTasks,
        Map<String, Long> tasksByStatus
) {}

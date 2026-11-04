package edu.eci.dosw.todo.dto;

import java.util.LinkedHashMap;
import java.util.Map;

// Solo métricas agregadas: no expone tareas, correos ni datos privados de nadie
public record AdminSummaryResponse(
        long totalUsers,
        long totalAdmins,
        long totalTasks,
        Map<String, Long> tasksByStatus
) {
    // Copia defensiva: nadie desde afuera puede modificar el mapa interno
    public AdminSummaryResponse {
        tasksByStatus = new LinkedHashMap<>(tasksByStatus);
    }

    @Override
    public Map<String, Long> tasksByStatus() {
        return new LinkedHashMap<>(tasksByStatus);
    }
}
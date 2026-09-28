package edu.eci.dosw.todo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity                     // esta clase se guarda en la base de datos
@Table(name = "tasks")      // nombre de la tabla en PostgreSQL
public class TaskEntity {

    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // la BD genera el id (autoincremental)
    private Long id;

    @Column(nullable = false, length = 120)  
    private String title;

    @Column(length = 500) 
    private String description;

    @Enumerated(EnumType.STRING)      
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(name = "due_date")         
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TaskEntity() {}            

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
package edu.eci.dosw.todo.entity;

import jakarta.persistence.*;

@Entity                     
@Table(name = "categorias")  
public class categoriasEntity {
    @Id                                                    
    @GeneratedValue(strategy = GenerationType.IDENTITY)   
    private Long id;

    @Column(nullable = false, length = 40)  
    private String nombre;

    @Column(length = 10) 
    private String color;

    public categoriasEntity() {}

     // Getters y setters
     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
 
     public String getNombre() { return nombre; }
     public void setNombre(String nombre) { this.nombre = nombre; }
 
     public String getColor() { return color; }
     public void setColor(String color) { this.color = color; }
 
}


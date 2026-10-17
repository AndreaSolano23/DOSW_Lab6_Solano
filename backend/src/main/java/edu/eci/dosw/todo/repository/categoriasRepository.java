package edu.eci.dosw.todo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.eci.dosw.todo.entity.categoriasEntity;

@Repository
public interface categoriasRepository extends JpaRepository<categoriasEntity,Long> {
}
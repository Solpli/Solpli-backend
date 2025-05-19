package com.ilta.solepli.domain.solelect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.solelect.entity.Solelect;

public interface SolelectRepository extends JpaRepository<Solelect, Long> {
  @EntityGraph(attributePaths = {"user", "solelectContents"})
  @Query("SELECT s FROM Solelect s WHERE s.id = :id AND s.deletedAt IS NULL")
  Optional<Solelect> findWithContentById(@Param("id") Long id);
}

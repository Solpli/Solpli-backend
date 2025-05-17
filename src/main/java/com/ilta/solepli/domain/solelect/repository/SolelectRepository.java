package com.ilta.solepli.domain.solelect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.solelect.entity.Solelect;

public interface SolelectRepository extends JpaRepository<Solelect, Long> {
  @EntityGraph(attributePaths = {"user", "solelectContents"})
  Optional<Solelect> findWithContentById(Long id);
}

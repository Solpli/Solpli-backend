package com.ilta.solepli.domain.solmark.sollect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.solmark.sollect.entity.SolmarkSollect;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkSollectRepository extends JpaRepository<SolmarkSollect, Long> {
  @Query("select s.sollect.id from SolmarkSollect s where s.user = :user")
  List<Long> findSollectIdsByUser(@Param("user") User user);

  Optional<SolmarkSollect> findBySollectAndUser(Sollect sollect, User user);

  Long countSolmarkSollectsBySollect(Sollect sollect);

  @Query(
      "SELECT s.sollect.id "
          + "FROM SolmarkSollect s "
          + "GROUP BY s.sollect.id "
          + "ORDER BY COUNT(s) DESC")
  List<Long> findPopularSollectIds(Pageable pageable);

  boolean existsBySollectIdAndUser(Long sollectId, User user);
}

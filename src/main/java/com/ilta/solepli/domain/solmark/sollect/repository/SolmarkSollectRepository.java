package com.ilta.solepli.domain.solmark.sollect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilta.solepli.domain.solmark.sollect.entity.SolmarkSollect;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkSollectRepository extends JpaRepository<SolmarkSollect, Long> {
  @Query("select s.sollect.id from SolmarkSollect s where s.user = :user")
  List<Long> findSollectIdsByUser(@Param("user") User user);
}

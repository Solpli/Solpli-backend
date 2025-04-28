package com.ilta.solepli.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {}

package com.ilta.solepli.domain.solmark.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkPlaceCollectionRepository
    extends JpaRepository<SolmarkPlaceCollection, Long> {

  Integer countByUser(User user);
}

package com.ilta.solepli.domain.solmark.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkPlaceCollectionRepository
    extends JpaRepository<SolmarkPlaceCollection, Long> {

  Integer countByUser(User user);

  List<SolmarkPlaceCollection> findByUserAndId_In(User user, List<Long> collectionIds);
}

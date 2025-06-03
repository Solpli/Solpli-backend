package com.ilta.solepli.domain.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ilta.solepli.domain.place.entity.SolmarkPlace;
import com.ilta.solepli.domain.user.entity.User;

@Repository
public interface SolmarkPlaceRepository extends JpaRepository<SolmarkPlace, Long> {

  List<SolmarkPlace> findAllByUserAndPlace_IdIn(User user, List<Long> placeIds);
}

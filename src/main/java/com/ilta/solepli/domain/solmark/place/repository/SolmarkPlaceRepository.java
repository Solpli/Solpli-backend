package com.ilta.solepli.domain.solmark.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlace;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkPlaceRepository extends JpaRepository<SolmarkPlace, Long> {

  List<SolmarkPlace> findBySolmarkPlaceList_UserAndPlace_idIn(
      User solmarkPlaceListUser, List<Long> placeIds);
}

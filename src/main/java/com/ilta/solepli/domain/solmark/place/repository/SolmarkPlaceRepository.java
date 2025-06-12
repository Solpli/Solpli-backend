package com.ilta.solepli.domain.solmark.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlace;
import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkPlaceRepository extends JpaRepository<SolmarkPlace, Long> {

  @Query(
      """
    SELECT sp
    FROM SolmarkPlace sp
    JOIN sp.solmarkPlaceCollection spc
    WHERE spc.user = :user
    AND sp.place.id IN :placeIds
    AND sp.deletedAt IS NULL
    AND spc.deletedAt IS NULL
""")
  List<SolmarkPlace> findAllNonDeletedByUserAndPlaceIds(User user, List<Long> placeIds);

  Boolean existsBySolmarkPlaceCollectionInAndPlace(
      List<SolmarkPlaceCollection> solmarkPlaceCollection, Place place);
}

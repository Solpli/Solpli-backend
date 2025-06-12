package com.ilta.solepli.domain.solmark.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ilta.solepli.domain.solmark.place.entity.SolmarkPlaceCollection;
import com.ilta.solepli.domain.user.entity.User;

public interface SolmarkPlaceCollectionRepository
    extends JpaRepository<SolmarkPlaceCollection, Long> {

  Integer countByUser(User user);

  @Query(
      """
    SELECT DISTINCT spc
    FROM SolmarkPlaceCollection spc
    JOIN FETCH spc.solmarkPlaces sp
    WHERE spc.user = :user AND spc.id IN :collectionIds
""")
  List<SolmarkPlaceCollection> findByUserAndId_In(User user, List<Long> collectionIds);

  @Query(
      """
      SELECT spc
      FROM SolmarkPlaceCollection spc
      LEFT JOIN FETCH spc.solmarkPlaces sp
      WHERE spc.user = :user
   """)
  List<SolmarkPlaceCollection> findByUser(User user);
}

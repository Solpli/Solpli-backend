package com.ilta.solepli.domain.solmark.place.repository;

import java.util.List;
import java.util.Optional;

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
    WHERE spc.user = :user AND spc.id IN :collectionIds AND spc.deletedAt IS NULL
""")
  List<SolmarkPlaceCollection> findByUserAndId_In(User user, List<Long> collectionIds);

  @Query(
      """
      SELECT spc
      FROM SolmarkPlaceCollection spc
      LEFT JOIN FETCH spc.solmarkPlaces sp
      WHERE spc.user = :user AND spc.deletedAt IS NULL
   """)
  List<SolmarkPlaceCollection> findByUser(User user);

  @Query(
      """
    SELECT spc
    FROM SolmarkPlaceCollection spc
    WHERE spc.user = :user
    AND spc.id = :id
    AND spc.deletedAt IS NULL
""")
  Optional<SolmarkPlaceCollection> findByIdAndUser(Long id, User user);

  @Query(
      """
    SELECT spc
    FROM SolmarkPlaceCollection spc
    WHERE spc.user = :user
    AND spc.id IN :collectionIds
    AND spc.deletedAt IS NULL
""")
  List<SolmarkPlaceCollection> findByUserAndIdInAndDeletedAtIsNull(
      User user, List<Long> collectionIds);

  @Query(
      """
    SELECT spc
    FROM SolmarkPlaceCollection spc
    JOIN FETCH spc.solmarkPlaces sp
    WHERE spc.user = :user
    AND spc.id IN :collectionIds
    AND spc.deletedAt IS NULL
""")
  List<SolmarkPlaceCollection> findByUserAndIdInAndDeletedAtIsNullWithPlaces(
      User user, List<Long> collectionIds);

  @Query(
      """
    SELECT spc.id
    FROM SolmarkPlace sp
    JOIN sp.solmarkPlaceCollection spc
    JOIN sp.place p
    WHERE spc.user = :user
    AND p.id = :placeId
    AND sp.deletedAt IS NULL
    AND spc.deletedAt IS NULL

""")
  List<Long> findByUserAndPlaceId(User user, Long placeId);
}

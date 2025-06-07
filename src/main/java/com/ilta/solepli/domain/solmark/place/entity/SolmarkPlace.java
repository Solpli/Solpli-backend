package com.ilta.solepli.domain.solmark.place.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.ilta.solepli.domain.place.entity.Place;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "solmark_places")
public class SolmarkPlace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "solmark_place_collection_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SolmarkPlaceCollection solmarkPlaceCollection;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Place place;
}

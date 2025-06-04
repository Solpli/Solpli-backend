package com.ilta.solepli.domain.solmark.place.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.ilta.solepli.domain.place.entity.Place;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.entity.Timestamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "solmark_places")
public class SolmarkPlace extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Place place;
}

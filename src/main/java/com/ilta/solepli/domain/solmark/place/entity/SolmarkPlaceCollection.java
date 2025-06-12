package com.ilta.solepli.domain.solmark.place.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.entity.Timestamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "solmark_place_collections")
public class SolmarkPlaceCollection extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @Column(nullable = false, length = 15)
  private String name;

  @Column(nullable = false)
  private int iconId;

  @OneToMany(mappedBy = "solmarkPlaceCollection", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<SolmarkPlace> solmarkPlaces = new ArrayList<>();
}

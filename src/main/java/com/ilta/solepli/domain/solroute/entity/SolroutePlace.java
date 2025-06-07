package com.ilta.solepli.domain.solroute.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.ilta.solepli.domain.place.entity.Place;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "solroute_places")
public class SolroutePlace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 100)
  private String memo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "solroute_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Solroute solroute;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Place place;

  public void setSolroute(Solroute solroute) {
    this.solroute = solroute;
  }
}

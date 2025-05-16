package com.ilta.solepli.domain.solelect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "solelect_contents")
public class SolelectContent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long seq;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentType type;

  private String text;

  private String imageUrl;

  @Builder.Default private Boolean isThumbnail = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "solelect_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Solelect solelect;

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void updateIsThumbnail(Boolean isThumbnail) {
    this.isThumbnail = isThumbnail;
  }
}

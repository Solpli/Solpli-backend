package com.ilta.solepli.domain.solroute.entity;

public enum SolrouteStatus {
  SCHEDULED("예정"),
  COMPLETED("완료");

  private final String description;

  SolrouteStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public SolrouteStatus update() {
    return this == SCHEDULED ? COMPLETED : SCHEDULED;
  }
}

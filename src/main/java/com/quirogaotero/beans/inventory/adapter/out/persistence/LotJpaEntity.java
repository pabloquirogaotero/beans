package com.quirogaotero.beans.inventory.adapter.out.persistence;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lots")
public class LotJpaEntity {

  @Id
  private UUID id;

  @Column(name = "sku_id", nullable = false)
  private String skuId;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "best_before", nullable = false)
  private LocalDate bestBefore;

  protected LotJpaEntity() { }

  public LotJpaEntity(UUID id, String skuId, String code, LocalDate bestBefore) {
    this.id = id;
    this.skuId = skuId;
    this.code = code;
    this.bestBefore = bestBefore;
  }

  public UUID getId() { return id; }
  public String getSkuId() { return skuId; }
  public String getCode() { return code; }
  public LocalDate getBestBefore() { return bestBefore; }

}
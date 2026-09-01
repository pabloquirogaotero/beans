package com.quirogaotero.beans.inventory.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "lot_stocks")
public class LotStockJpaEntity {

  @EmbeddedId
  private LotStockKey id;

  @Column(name = "on_hand", nullable = false)
  private int onHand;

  @Column(nullable = false)
  private int reserved;

  @Version
  @Column(nullable = false)
  private long version;

  protected LotStockJpaEntity() { }

  public LotStockJpaEntity(LotStockKey id, int onHand, int reserved, long version) {
    this.id = id;
    this.onHand = onHand;
    this.reserved = reserved;
    this.version = version;
  }

  public LotStockKey getId() { return id; }
  public int getOnHand() { return onHand; }
  public int getReserved() { return reserved; }
  public long getVersion() { return version; }

  public void setOnHand(int onHand) { this.onHand = onHand; }
  public void setReserved(int reserved) { this.reserved = reserved; }
}

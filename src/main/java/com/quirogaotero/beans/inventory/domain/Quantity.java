package com.quirogaotero.beans.inventory.domain;

public record Quantity(int value) {

  public Quantity {
    if (value < 0)
      throw new IllegalArgumentException("Quantity must not be negative.");
  }

  public static final Quantity ZERO = new Quantity(0);

  public static Quantity of(int value) { return new Quantity(value); }

  public Quantity plus(Quantity other) { return new Quantity(value + other.value); }
  public Quantity minus(Quantity other) { return new Quantity(value - other.value); }
  public boolean isGreaterThan(Quantity other) { return value > other.value; }
  public boolean isZero() { return value == 0; }

}

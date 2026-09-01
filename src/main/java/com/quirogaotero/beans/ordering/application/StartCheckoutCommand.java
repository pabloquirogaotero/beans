package com.quirogaotero.beans.ordering.application;

import java.util.List;

public record StartCheckoutCommand(double latitude, double longitude, List<Line> lines) {

  public record Line(String sku, int quantity) {}

}
package com.quirogaotero.beans.inventory.adapter.out.persistence;

import java.time.LocalDate;
import java.util.UUID;

public interface CandidateRow {

  String getLocationId();

  String getLocationType();

  double getLatitude();

  double getLongitude();

  UUID getLotId();

  String getSkuId();

  String getLotCode();

  LocalDate getBestBefore();

  int getAvailable();

}
package com.quirogaotero.beans.inventory.adapter.in.scheduling;

import com.quirogaotero.beans.inventory.application.port.in.ExpireReservationsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryJob {

  private final ExpireReservationsUseCase expireReservations;

  public ReservationExpiryJob(ExpireReservationsUseCase expireReservations) {
    this.expireReservations = expireReservations;
  }

  @Scheduled(fixedDelayString = "${beans.checkout.expiry-sweep-interval:PT1M}")
  public void sweep() {
    expireReservations.expireReservations();
  }

}
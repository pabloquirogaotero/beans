package com.quirogaotero.beans.inventory.application.service;

import com.quirogaotero.beans.inventory.application.port.in.*;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.ThreadLocalRandom;

public class RetryingReserveForCheckout implements ReserveForCheckoutUseCase {

  private final ReserveForCheckoutUseCase delegate;
  private final int maxAttempts;
  private final long baseBackoffMillis;

  public RetryingReserveForCheckout(ReserveForCheckoutUseCase delegate,
                                    int maxAttempts, long baseBackoffMillis) {
    this.delegate = delegate;
    this.maxAttempts = maxAttempts;
    this.baseBackoffMillis = baseBackoffMillis;
  }

  @Override
  public ReservationResult reserveForCheckout(ReserveForCheckoutCommand command) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return delegate.reserveForCheckout(command);
      } catch (OptimisticLockingFailureException lostRace) {
        if (attempt == maxAttempts) {
          return ReservationResult.newFailedReservation("Could not secure stock under contention");
        }
        backoffWithJitter(attempt);
      }
    }
    return ReservationResult.newFailedReservation("Could not secure stock under contention");
  }

  private void backoffWithJitter(int attempt) {
    long jitter = ThreadLocalRandom.current().nextLong(baseBackoffMillis * attempt);
    try {
      Thread.sleep(jitter);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
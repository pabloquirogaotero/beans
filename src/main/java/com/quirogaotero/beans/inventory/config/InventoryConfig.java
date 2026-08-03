package com.quirogaotero.beans.inventory.config;

import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutUseCase;
import com.quirogaotero.beans.inventory.application.port.out.*;
import com.quirogaotero.beans.inventory.application.service.ReserveForCheckoutService;
import com.quirogaotero.beans.inventory.domain.AllocationStrategy;
import com.quirogaotero.beans.inventory.domain.ConsolidatedAllocationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class InventoryConfig {

  @Bean
  AllocationStrategy allocationStrategy() {
    return new ConsolidatedAllocationStrategy();
  }

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  ReserveForCheckoutUseCase reserveForCheckoutUseCase(
          CandidateFinder candidateFinder,
          LotStockRepository lotStocks,
          ReservationRepository reservations,
          AllocationStrategy allocationStrategy,
          Clock clock,
          @Value("${beans.checkout.hold-duration:PT15M}") Duration holdDuration,
          @Value("${beans.checkout.min-shelf-life-days:30}") int minShelfLifeDays) {

    return new ReserveForCheckoutService(candidateFinder, lotStocks, reservations,
            allocationStrategy, clock, holdDuration, minShelfLifeDays);
  }

}

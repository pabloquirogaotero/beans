package com.quirogaotero.beans.inventory.config;

import com.quirogaotero.beans.inventory.application.port.in.ConfirmReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ExpireReservationsUseCase;
import com.quirogaotero.beans.inventory.application.port.in.PickForOrderUseCase;
import com.quirogaotero.beans.inventory.application.port.in.ReserveForCheckoutUseCase;
import com.quirogaotero.beans.inventory.application.port.out.*;
import com.quirogaotero.beans.inventory.application.service.ConfirmReservationsService;
import com.quirogaotero.beans.inventory.application.service.ExpireReservationsService;
import com.quirogaotero.beans.inventory.application.service.PickForOrderService;
import com.quirogaotero.beans.inventory.application.service.ReserveForCheckoutService;
import com.quirogaotero.beans.inventory.application.service.RetryingReserveForCheckout;
import com.quirogaotero.beans.inventory.domain.AllocationStrategy;
import com.quirogaotero.beans.inventory.domain.ConsolidatedAllocationStrategy;
import com.quirogaotero.beans.messaging.application.port.out.ProcessedEvents;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;

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
  ReserveForCheckoutService reserveForCheckoutCore(
          CandidateFinder candidateFinder, LotStockRepository lotStocks,
          ReservationRepository reservations, AllocationStrategy allocationStrategy, Clock clock,
          @Value("${beans.checkout.hold-duration:PT15M}") Duration holdDuration,
          @Value("${beans.checkout.min-shelf-life-days:30}") int minShelfLifeDays) {
    return new ReserveForCheckoutService(candidateFinder, lotStocks, reservations,
            allocationStrategy, clock, holdDuration, minShelfLifeDays);
  }

  @Bean
  @Primary
  ReserveForCheckoutUseCase reserveForCheckoutUseCase(ReserveForCheckoutService core) {
    return new RetryingReserveForCheckout(core, 5, 20);
  }

  @Bean
  ExpireReservationsUseCase expireReservationsUseCase(
          ReservationRepository reservations, LotStockRepository lotStocks, Clock clock) {
    return new ExpireReservationsService(reservations, lotStocks, clock);
  }

  @Bean
  ConfirmReservationsUseCase confirmReservationsUseCase(ReservationRepository reservations, Clock clock) {
    return new ConfirmReservationsService(reservations, clock);
  }

  @Bean
  PickForOrderUseCase pickForOrderUseCase(ReservationRepository reservations, LotStockRepository lotStocks,
                                          ProcessedEvents processedEvents, InventoryOutbox outbox, Clock clock) {
    return new PickForOrderService(reservations, lotStocks, processedEvents, outbox, clock);
  }

  @Bean
  NewTopic stockEventsTopic(@Value("${beans.messaging.topics.stock-events}") String topic) {
    return TopicBuilder.name(topic).partitions(3).replicas(1).build();
  }

}

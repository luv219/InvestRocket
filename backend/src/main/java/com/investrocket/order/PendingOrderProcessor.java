package com.investrocket.order;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.pending-order-processor",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PendingOrderProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingOrderProcessor.class);

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public PendingOrderProcessor(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${app.pending-order-processor.interval-ms:30000}")
    public void processPendingOrders() {
        for (UUID orderId : orderRepository.findIdsByStatus(OrderStatus.PENDING)) {
            try {
                orderService.executePendingOrder(orderId);
            } catch (Exception exception) {
                LOGGER.warn("Unable to process pending order {}", orderId, exception);
            }
        }
    }
}

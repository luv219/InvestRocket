package com.investrocket.order;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingOrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Test
    void processesEveryPendingOrderId() {
        UUID firstOrderId = UUID.randomUUID();
        UUID secondOrderId = UUID.randomUUID();
        when(orderRepository.findIdsByStatus(OrderStatus.PENDING))
                .thenReturn(List.of(firstOrderId, secondOrderId));

        PendingOrderProcessor processor =
                new PendingOrderProcessor(orderRepository, orderService);

        processor.processPendingOrders();

        verify(orderService).executePendingOrder(firstOrderId);
        verify(orderService).executePendingOrder(secondOrderId);
    }
}

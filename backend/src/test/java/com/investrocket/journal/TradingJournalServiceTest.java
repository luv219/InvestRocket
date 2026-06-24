package com.investrocket.journal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.JournalLinkNotFoundException;
import com.investrocket.journal.dto.CreateJournalEntryRequest;
import com.investrocket.order.OrderRepository;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;

class TradingJournalServiceTest {

    @Test
    void rejectsOrderThatDoesNotBelongToCurrentUser() {
        TradingJournalRepository journalRepository =
                Mockito.mock(TradingJournalRepository.class);
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        TradeRepository tradeRepository = Mockito.mock(TradeRepository.class);
        User user = Mockito.mock(User.class);
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUser(orderId, user)).thenReturn(Optional.empty());
        TradingJournalService service = new TradingJournalService(
                journalRepository, orderRepository, tradeRepository,
                Mockito.mock(AuditLogService.class));

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "Plan", "Testing plan", JournalMood.NEUTRAL,
                null, "AAPL", orderId, null, null);

        assertThrows(
                JournalLinkNotFoundException.class,
                () -> service.createEntry(request, user));
    }
}

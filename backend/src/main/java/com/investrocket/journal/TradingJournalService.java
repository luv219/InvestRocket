package com.investrocket.journal;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.JournalEntryNotFoundException;
import com.investrocket.exception.JournalLinkNotFoundException;
import com.investrocket.journal.dto.CreateJournalEntryRequest;
import com.investrocket.journal.dto.JournalEntryResponse;
import com.investrocket.journal.dto.UpdateJournalEntryRequest;
import com.investrocket.order.Order;
import com.investrocket.order.OrderRepository;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;

@Service
public class TradingJournalService {

    private final TradingJournalRepository journalRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final AuditLogService auditLogService;

    public TradingJournalService(
            TradingJournalRepository journalRepository,
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            AuditLogService auditLogService) {
        this.journalRepository = journalRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public JournalEntryResponse createEntry(
            CreateJournalEntryRequest request,
            User user) {
        Order order = request.orderId() == null ? null
                : orderRepository.findByIdAndUser(request.orderId(), user)
                        .orElseThrow(() -> new JournalLinkNotFoundException(
                                "Linked order not found"));
        Trade trade = request.tradeId() == null ? null
                : tradeRepository.findByIdAndUser(request.tradeId(), user)
                        .orElseThrow(() -> new JournalLinkNotFoundException(
                                "Linked trade not found"));
        TradingJournalEntry entry = journalRepository.save(new TradingJournalEntry(
                user, request.title().trim(), request.content().trim(),
                request.mood(), normalize(request.strategy()),
                symbol(request.symbol()), order, trade, normalize(request.tags())));
        auditLogService.log(
                user, AuditCategory.SYSTEM, AuditAction.JOURNAL_ENTRY_CREATED,
                "Trading journal entry created");
        return JournalEntryResponse.from(entry);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryResponse> getMyEntries(User user) {
        return journalRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(JournalEntryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<JournalEntryResponse> getEntriesBySymbol(String symbol, User user) {
        return journalRepository
                .findByUserAndSymbolOrderByCreatedAtDesc(user, symbol(symbol))
                .stream().map(JournalEntryResponse::from).toList();
    }

    @Transactional
    public JournalEntryResponse updateEntry(
            UUID entryId,
            UpdateJournalEntryRequest request,
            User user) {
        TradingJournalEntry entry = journalRepository.findByIdAndUser(entryId, user)
                .orElseThrow(JournalEntryNotFoundException::new);
        entry.update(
                request.title().trim(), request.content().trim(), request.mood(),
                normalize(request.strategy()), symbol(request.symbol()),
                normalize(request.tags()));
        auditLogService.log(
                user, AuditCategory.SYSTEM, AuditAction.JOURNAL_ENTRY_UPDATED,
                "Trading journal entry updated");
        return JournalEntryResponse.from(entry);
    }

    @Transactional
    public void deleteEntry(UUID entryId, User user) {
        TradingJournalEntry entry = journalRepository.findByIdAndUser(entryId, user)
                .orElseThrow(JournalEntryNotFoundException::new);
        journalRepository.delete(entry);
        auditLogService.log(
                user, AuditCategory.SYSTEM, AuditAction.JOURNAL_ENTRY_DELETED,
                "Trading journal entry deleted");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String symbol(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}

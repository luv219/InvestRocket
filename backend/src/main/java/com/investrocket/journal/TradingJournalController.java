package com.investrocket.journal;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.journal.dto.CreateJournalEntryRequest;
import com.investrocket.journal.dto.JournalEntryResponse;
import com.investrocket.journal.dto.UpdateJournalEntryRequest;
import com.investrocket.user.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/journal")
public class TradingJournalController {

    private final TradingJournalService journalService;
    private final CurrentUserService currentUserService;

    public TradingJournalController(
            TradingJournalService journalService,
            CurrentUserService currentUserService) {
        this.journalService = journalService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryResponse>> create(
            @Valid @RequestBody CreateJournalEntryRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Journal entry created",
                journalService.createEntry(request, currentUser(principal))));
    }

    @GetMapping
    public ApiResponse<List<JournalEntryResponse>> getEntries(
            @RequestParam(required = false) String symbol,
            Principal principal) {
        List<JournalEntryResponse> entries = symbol == null || symbol.isBlank()
                ? journalService.getMyEntries(currentUser(principal))
                : journalService.getEntriesBySymbol(symbol, currentUser(principal));
        return ApiResponse.success("Journal entries fetched successfully", entries);
    }

    @PutMapping("/{entryId}")
    public ApiResponse<JournalEntryResponse> update(
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateJournalEntryRequest request,
            Principal principal) {
        return ApiResponse.success("Journal entry updated",
                journalService.updateEntry(entryId, request, currentUser(principal)));
    }

    @DeleteMapping("/{entryId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID entryId,
            Principal principal) {
        journalService.deleteEntry(entryId, currentUser(principal));
        return ApiResponse.success("Journal entry deleted", null);
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}

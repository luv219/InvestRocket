package com.investrocket.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.investrocket.analytics.AnalyticsService;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.InvalidCurrentPasswordException;
import com.investrocket.exception.InvalidResetConfirmationException;
import com.investrocket.order.OrderService;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.user.dto.ChangePasswordRequest;
import com.investrocket.user.dto.ResetAccountRequest;
import com.investrocket.user.dto.UpdateProfileRequest;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    private UserProfileService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(
                userRepository,
                walletRepository,
                holdingRepository,
                orderService,
                analyticsService,
                passwordEncoder,
                auditLogService);
        user = new User("Demo User", "demo@example.com", "old-hash");
    }

    @Test
    void updatesProfileWithoutChangingEmail() {
        var response = service.updateProfile(
                new UpdateProfileRequest("Updated User", "12345", "India", "inr"),
                user);

        assertThat(response.fullName()).isEqualTo("Updated User");
        assertThat(response.email()).isEqualTo("demo@example.com");
        assertThat(response.preferredCurrency()).isEqualTo("INR");
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(
                new ChangePasswordRequest("wrong", "Password456", "Password456"),
                user))
                .isInstanceOf(InvalidCurrentPasswordException.class);
    }

    @Test
    void changesPasswordAfterVerification() {
        when(passwordEncoder.matches("Password123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("Password456")).thenReturn("new-hash");

        service.changePassword(
                new ChangePasswordRequest(
                        "Password123",
                        "Password456",
                        "Password456"),
                user);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void rejectsInvalidResetConfirmation() {
        assertThatThrownBy(() -> service.resetSimulator(
                new ResetAccountRequest("RESET"),
                user))
                .isInstanceOf(InvalidResetConfirmationException.class);
    }

    @Test
    void resetsWalletHoldingsAndPendingOrders() {
        Wallet wallet = new Wallet(user);
        wallet.debit(new java.math.BigDecimal("500.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));

        service.resetSimulator(
                new ResetAccountRequest("RESET MY SIMULATOR"),
                user);

        assertThat(wallet.getCashBalance()).isEqualByComparingTo("100000.00");
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("0.00");
        verify(orderService).cancelPendingOrdersForReset(user);
        verify(holdingRepository).deleteByUser(user);
        verify(analyticsService).createSnapshot(user);
    }
}

package com.investrocket.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.admin.dto.AdminUserUpdateRequest;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.AdminSelfProtectionException;
import com.investrocket.order.OrderRepository;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.portfolio.PortfolioService;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.user.Role;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.WalletRepository;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private PortfolioService portfolioService;
    @Mock private RiskSettingsService riskSettingsService;
    @Mock private AuditLogService auditLogService;

    private AdminUserService service;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(
                userRepository,
                walletRepository,
                orderRepository,
                tradeRepository,
                holdingRepository,
                portfolioService,
                riskSettingsService,
                auditLogService);
        admin = new User("Admin", "admin@example.com", "hash");
        admin.updateAdminFields("Admin", Role.ADMIN, true);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
    }

    @Test
    void preventsAdminFromDisablingSelf() {
        assertThatThrownBy(() -> service.disableUser(admin.getId(), admin))
                .isInstanceOf(AdminSelfProtectionException.class);
    }

    @Test
    void preventsAdminFromRemovingOwnRole() {
        assertThatThrownBy(() -> service.updateUser(
                admin.getId(),
                new AdminUserUpdateRequest("Admin", Role.USER, true),
                admin))
                .isInstanceOf(AdminSelfProtectionException.class);
    }
}

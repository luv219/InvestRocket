package com.investrocket.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.admin.dto.AdminOrderResponse;
import com.investrocket.admin.dto.AdminTradeResponse;
import com.investrocket.admin.dto.AdminUserDetailResponse;
import com.investrocket.admin.dto.AdminUserResponse;
import com.investrocket.admin.dto.AdminUserUpdateRequest;
import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.audit.dto.ActivityLogResponse;
import com.investrocket.exception.AdminSelfProtectionException;
import com.investrocket.exception.UserNotFoundException;
import com.investrocket.order.OrderRepository;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.portfolio.PortfolioService;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.user.Role;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.user.dto.UserProfileResponse;
import com.investrocket.wallet.WalletRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioService portfolioService;
    private final RiskSettingsService riskSettingsService;
    private final AuditLogService auditLogService;

    public AdminUserService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            HoldingRepository holdingRepository,
            PortfolioService portfolioService,
            RiskSettingsService riskSettingsService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.holdingRepository = holdingRepository;
        this.portfolioService = portfolioService;
        this.riskSettingsService = riskSettingsService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminUserDetailResponse getUserById(UUID userId) {
        User user = requireUser(userId);
        return new AdminUserDetailResponse(
                UserProfileResponse.from(user),
                toResponse(user),
                riskSettingsService.getRiskSettings(user),
                portfolioService.getPortfolioSummary(user),
                orderRepository.findByUserOrderByCreatedAtDesc(user)
                        .stream()
                        .limit(20)
                        .map(AdminOrderResponse::from)
                        .toList(),
                tradeRepository.findByUserOrderByExecutedAtDesc(user)
                        .stream()
                        .limit(20)
                        .map(AdminTradeResponse::from)
                        .toList(),
                auditLogService.getMyActivity(user).stream().limit(20).toList());
    }

    @Transactional
    public AdminUserResponse updateUser(
            UUID userId,
            AdminUserUpdateRequest request,
            User currentAdmin) {
        User target = requireUser(userId);
        protectCurrentAdmin(target, request.role(), request.enabled(), currentAdmin);
        target.updateAdminFields(
                request.fullName().trim(),
                request.role(),
                request.enabled());
        auditAdminChange(
                currentAdmin,
                AuditAction.ADMIN_USER_UPDATED,
                "Admin updated user " + target.getEmail());
        return toResponse(target);
    }

    @Transactional
    public AdminUserResponse disableUser(UUID userId, User currentAdmin) {
        User target = requireUser(userId);
        if (target.getId().equals(currentAdmin.getId())) {
            throw new AdminSelfProtectionException(
                    "You cannot disable your own admin account");
        }
        target.setEnabled(false);
        auditAdminChange(
                currentAdmin,
                AuditAction.ADMIN_USER_DISABLED,
                "Admin disabled user " + target.getEmail());
        return toResponse(target);
    }

    @Transactional
    public AdminUserResponse enableUser(UUID userId, User currentAdmin) {
        User target = requireUser(userId);
        target.setEnabled(true);
        auditAdminChange(
                currentAdmin,
                AuditAction.ADMIN_USER_ENABLED,
                "Admin enabled user " + target.getEmail());
        return toResponse(target);
    }

    private AdminUserResponse toResponse(User user) {
        BigDecimal cash = walletRepository.findByUser(user)
                .map(wallet -> wallet.getCashBalance().add(wallet.getReservedBalance()))
                .orElse(BigDecimal.ZERO.setScale(2));
        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                cash,
                orderRepository.countByUser(user),
                tradeRepository.countByUser(user),
                holdingRepository.countByUser(user));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void protectCurrentAdmin(
            User target,
            Role requestedRole,
            boolean requestedEnabled,
            User currentAdmin) {
        if (!target.getId().equals(currentAdmin.getId())) {
            return;
        }
        if (!requestedEnabled) {
            throw new AdminSelfProtectionException(
                    "You cannot disable your own admin account");
        }
        if (requestedRole != Role.ADMIN) {
            throw new AdminSelfProtectionException(
                    "You cannot remove your own ADMIN role");
        }
    }

    private void auditAdminChange(
            User currentAdmin,
            AuditAction action,
            String description) {
        auditLogService.log(
                currentAdmin,
                AuditCategory.SYSTEM,
                action,
                description);
    }
}

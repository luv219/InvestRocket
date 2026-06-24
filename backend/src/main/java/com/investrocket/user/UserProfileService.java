package com.investrocket.user;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.investrocket.analytics.AnalyticsService;
import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.InvalidCurrentPasswordException;
import com.investrocket.exception.InvalidResetConfirmationException;
import com.investrocket.exception.PasswordMismatchException;
import com.investrocket.exception.WalletNotFoundException;
import com.investrocket.order.OrderService;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.user.dto.ChangePasswordRequest;
import com.investrocket.user.dto.ResetAccountRequest;
import com.investrocket.user.dto.UpdateProfileRequest;
import com.investrocket.user.dto.UserProfileResponse;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Service
public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);
    private static final String RESET_CONFIRMATION = "RESET MY SIMULATOR";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final OrderService orderService;
    private final AnalyticsService analyticsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserProfileService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            HoldingRepository holdingRepository,
            OrderService orderService,
            AnalyticsService analyticsService,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.holdingRepository = holdingRepository;
        this.orderService = orderService;
        this.analyticsService = analyticsService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile(User currentUser) {
        return UserProfileResponse.from(currentUser);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            UpdateProfileRequest request,
            User currentUser) {
        currentUser.updateProfile(
                request.fullName().trim(),
                normalizeOptional(request.phoneNumber()),
                normalizeOptional(request.country()),
                request.preferredCurrency() == null
                        ? "USD"
                        : request.preferredCurrency().toUpperCase(Locale.ROOT));
        userRepository.save(currentUser);
        auditLogService.log(
                currentUser,
                AuditCategory.PROFILE,
                AuditAction.PROFILE_UPDATED,
                "Profile information updated");
        return UserProfileResponse.from(currentUser);
    }

    @Transactional
    public void changePassword(
            ChangePasswordRequest request,
            User currentUser) {
        if (!passwordEncoder.matches(
                request.currentPassword(),
                currentUser.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new PasswordMismatchException(
                    "New password and confirmation do not match");
        }
        currentUser.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
        auditLogService.log(
                currentUser,
                AuditCategory.PROFILE,
                AuditAction.PASSWORD_CHANGED,
                "Account password changed");
    }

    @Transactional
    public void resetSimulator(
            ResetAccountRequest request,
            User currentUser) {
        if (!RESET_CONFIRMATION.equals(request.confirmText())) {
            throw new InvalidResetConfirmationException();
        }
        orderService.cancelPendingOrdersForReset(currentUser);
        holdingRepository.deleteByUser(currentUser);
        Wallet wallet = walletRepository.findForUpdateByUser(currentUser)
                .orElseThrow(WalletNotFoundException::new);
        wallet.reset();
        auditLogService.log(
                currentUser,
                AuditCategory.WALLET,
                AuditAction.SIMULATOR_RESET,
                "Virtual simulator balance and holdings reset");
        createSnapshotAfterCommit(currentUser);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void createSnapshotAfterCommit(User currentUser) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            analyticsService.createSnapshot(currentUser);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            analyticsService.createSnapshot(currentUser);
                        } catch (RuntimeException exception) {
                            LOGGER.warn(
                                    "Unable to create post-reset snapshot for user {}",
                                    currentUser.getId());
                        }
                    }
                });
    }
}

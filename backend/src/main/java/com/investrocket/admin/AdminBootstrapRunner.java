package com.investrocket.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.user.Role;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Component
@ConditionalOnProperty(
        prefix = "app.admin-bootstrap",
        name = "enabled",
        havingValue = "true")
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RiskSettingsService riskSettingsService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            WalletRepository walletRepository,
            RiskSettingsService riskSettingsService,
            AuditLogService auditLogService,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin-bootstrap.email:}") String email,
            @Value("${app.admin-bootstrap.password:}") String password,
            @Value("${app.admin-bootstrap.full-name:Invest Rocket Admin}") String fullName) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.riskSettingsService = riskSettingsService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.length() < 8) {
            LOGGER.error(
                    "Admin bootstrap enabled but ADMIN_EMAIL or ADMIN_PASSWORD is invalid");
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }
        User admin = new User(
                fullName.trim(),
                normalizedEmail,
                passwordEncoder.encode(password));
        admin.updateAdminFields(fullName.trim(), Role.ADMIN, true);
        User savedAdmin = userRepository.saveAndFlush(admin);
        walletRepository.save(new Wallet(savedAdmin));
        riskSettingsService.createDefaults(savedAdmin);
        auditLogService.logWithinCurrentTransaction(
                savedAdmin,
                AuditCategory.SYSTEM,
                AuditAction.ADMIN_BOOTSTRAPPED,
                "Initial administrator account bootstrapped");
    }
}

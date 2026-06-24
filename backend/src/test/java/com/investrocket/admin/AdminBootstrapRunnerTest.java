package com.investrocket.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.investrocket.audit.AuditLogService;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.user.Role;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private RiskSettingsService riskSettingsService;
    @Mock private AuditLogService auditLogService;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminOnceWithHashedPassword() throws Exception {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePassword123")).thenReturn("hash");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                userRepository,
                walletRepository,
                riskSettingsService,
                auditLogService,
                passwordEncoder,
                "admin@example.com",
                "SecurePassword123",
                "Invest Rocket Admin");

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(passwordEncoder).encode("SecurePassword123");
        verify(userRepository).saveAndFlush(any(User.class));
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }
}

package com.investrocket.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.investrocket.auth.dto.AuthResponse;
import com.investrocket.auth.dto.LoginRequest;
import com.investrocket.auth.dto.RegisterRequest;
import com.investrocket.exception.DuplicateEmailException;
import com.investrocket.exception.PasswordMismatchException;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                walletRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);
    }

    @Test
    void registersUserAndCreatesDefaultWallet() {
        RegisterRequest request = new RegisterRequest(
                "Demo User",
                "Demo@Example.com",
                "Password123",
                "Password123");
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("demo@example.com");

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertThat(walletCaptor.getValue().getCashBalance()).isEqualByComparingTo("100000.00");
        assertThat(walletCaptor.getValue().getCurrency()).isEqualTo("USD");
    }

    @Test
    void rejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Demo User",
                "demo@example.com",
                "Password123",
                "Password123");
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void rejectsPasswordMismatch() {
        RegisterRequest request = new RegisterRequest(
                "Demo User",
                "demo@example.com",
                "Password123",
                "Different123");
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Test
    void logsInExistingUser() {
        User user = new User("Demo User", "demo@example.com", "hashed-password");
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(
                new LoginRequest("demo@example.com", "Password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().fullName()).isEqualTo("Demo User");
        verify(authenticationManager).authenticate(any());
    }
}

package com.investrocket.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.auth.dto.AuthResponse;
import com.investrocket.auth.dto.LoginRequest;
import com.investrocket.auth.dto.RegisterRequest;
import com.investrocket.auth.dto.UserResponse;
import com.investrocket.exception.DuplicateEmailException;
import com.investrocket.exception.InvalidCredentialsException;
import com.investrocket.exception.PasswordMismatchException;
import com.investrocket.exception.UserNotFoundException;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private RiskSettingsService riskSettingsService;
    private AuditLogService auditLogService;

    public AuthService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Autowired
    void setRiskSettingsService(RiskSettingsService riskSettingsService) {
        this.riskSettingsService = riskSettingsService;
    }

    @Autowired
    void setAuditLogService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException();
        }

        User user = new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()));
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
        walletRepository.save(new Wallet(savedUser));
        if (riskSettingsService != null) {
            riskSettingsService.createDefaults(savedUser);
        }
        if (auditLogService != null) {
            auditLogService.logWithinCurrentTransaction(
                    savedUser,
                    AuditCategory.AUTH,
                    AuditAction.USER_REGISTERED,
                    "User account registered");
        }

        return AuthResponse.bearer(jwtService.generateToken(savedUser), UserResponse.from(savedUser));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        user.recordLogin();
        userRepository.save(user);
        if (auditLogService != null) {
            auditLogService.log(
                    user,
                    AuditCategory.AUTH,
                    AuditAction.USER_LOGGED_IN,
                    "User logged in");
        }
        return AuthResponse.bearer(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

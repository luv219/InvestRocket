package com.investrocket.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.investrocket.user.User;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test_secret_that_is_at_least_32_characters_long",
            60_000);

    @Test
    void generatesValidTokenForUser() {
        User user = new User("Demo User", "demo@example.com", "hashed-password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("demo@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}

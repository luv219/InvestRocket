package com.investrocket.auth;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.auth.dto.AuthResponse;
import com.investrocket.auth.dto.LoginRequest;
import com.investrocket.auth.dto.RegisterRequest;
import com.investrocket.auth.dto.UserResponse;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;
import com.investrocket.user.Role;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void registersValidUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Demo User",
                "demo@example.com",
                "Password123",
                "Password123");
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "Demo User",
                "demo@example.com",
                Role.USER);
        when(authService.register(request))
                .thenReturn(AuthResponse.bearer("jwt-token", userResponse));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Demo User",
                                  "email": "demo@example.com",
                                  "password": "Password123",
                                  "confirmPassword": "Password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("demo@example.com"));
    }

    @Test
    void rejectsInvalidRegistrationPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "email": "invalid",
                                  "password": "short",
                                  "confirmPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void logsInValidUser() throws Exception {
        LoginRequest request = new LoginRequest("demo@example.com", "Password123");
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "Demo User",
                "demo@example.com",
                Role.USER);
        when(authService.login(request))
                .thenReturn(AuthResponse.bearer("jwt-token", userResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void returnsCurrentAuthenticatedUser() throws Exception {
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "Demo User",
                "demo@example.com",
                Role.USER);
        when(authService.getCurrentUser("demo@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me").with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Demo User"));
    }

    @Test
    void rejectsUnauthenticatedCurrentUserRequest() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }
}

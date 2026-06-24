package com.investrocket.user;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.audit.AuditController;
import com.investrocket.audit.AuditLogService;
import com.investrocket.auth.JwtAuthenticationFilter;
import com.investrocket.auth.JwtService;
import com.investrocket.common.CurrentUserService;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;
import com.investrocket.user.dto.RiskSettingsResponse;
import com.investrocket.user.dto.UserProfileResponse;

@WebMvcTest({
        UserProfileController.class,
        RiskSettingsController.class,
        AuditController.class
})
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class AccountSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private RiskSettingsService riskSettingsService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedPhaseSevenEndpoints() throws Exception {
        mockMvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile/risk-settings"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/activity")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/profile/reset-simulator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"RESET MY SIMULATOR\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsOnlyAuthenticatedUsersProfileAndSettings() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(userProfileService.getCurrentProfile(currentUser))
                .thenReturn(UserProfileResponse.from(currentUser));
        when(riskSettingsService.getRiskSettings(currentUser))
                .thenReturn(new RiskSettingsResponse(
                        new BigDecimal("25000.00"),
                        50,
                        true,
                        true));

        mockMvc.perform(get("/api/profile").with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("demo@example.com"));
        mockMvc.perform(get("/api/profile/risk-settings")
                        .with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxDailyTrades").value(50));
    }

    @Test
    void filtersAuthenticatedUsersActivity() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(auditLogService.getMyActivityByCategory(currentUser, "ORDER"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/activity")
                        .param("category", "ORDER")
                        .with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}

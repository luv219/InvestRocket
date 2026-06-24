package com.investrocket.analytics;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.analytics.dto.PortfolioPerformancePoint;
import com.investrocket.common.CurrentUserService;
import com.investrocket.auth.JwtAuthenticationFilter;
import com.investrocket.auth.JwtService;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;
import com.investrocket.user.User;

@WebMvcTest(AnalyticsController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedAnalyticsRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/performance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsAuthenticatedPerformanceHistory() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(analyticsService.getPerformanceHistory(currentUser))
                .thenReturn(List.of(point()));

        mockMvc.perform(get("/api/analytics/performance")
                        .with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalPortfolioValue").value(104250.00));
    }

    @Test
    void createsSnapshotForAuthenticatedUser() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(analyticsService.createSnapshot(currentUser)).thenReturn(point());

        mockMvc.perform(post("/api/analytics/snapshot")
                        .with(user("demo@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Portfolio snapshot created successfully"));
    }

    private PortfolioPerformancePoint point() {
        return new PortfolioPerformancePoint(
                LocalDate.parse("2026-06-24"),
                Instant.parse("2026-06-24T16:00:00Z"),
                new BigDecimal("104250.00"),
                new BigDecimal("51250.00"),
                new BigDecimal("53000.00"),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2));
    }
}

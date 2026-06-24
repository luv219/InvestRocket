package com.investrocket.admin;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.admin.dto.AdminDashboardStatsResponse;
import com.investrocket.auth.JwtAuthenticationFilter;
import com.investrocket.auth.JwtService;
import com.investrocket.common.CurrentUserService;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;

@WebMvcTest({AdminMonitoringController.class, AdminUserController.class})
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminMonitoringService adminMonitoringService;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedAdminRequest() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsNormalUserFromAdminRequest() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminRole() throws Exception {
        when(adminMonitoringService.getDashboardStats()).thenReturn(stats());

        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(2));
    }

    private AdminDashboardStatsResponse stats() {
        return new AdminDashboardStatsResponse(
                2, 2, 0, 1, 3, 2, 1, 0, 0, 2, 1, 1,
                new BigDecimal("150000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("200000.00"),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2));
    }
}

package com.investrocket.order;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.auth.JwtAuthenticationFilter;
import com.investrocket.auth.JwtService;
import com.investrocket.common.CurrentUserService;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;

@WebMvcTest(OrderController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedOrderHistory() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedPendingOrders() throws Exception {
        mockMvc.perform(get("/api/orders/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedCancellation() throws Exception {
        mockMvc.perform(delete("/api/orders/{orderId}/cancel", java.util.UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesOrderRequestBeforeExecution() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(user("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "",
                                  "side": "BUY",
                                  "orderType": "MARKET",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.symbol").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }
}

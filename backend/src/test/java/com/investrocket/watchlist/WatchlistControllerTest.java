package com.investrocket.watchlist;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.investrocket.user.User;
import com.investrocket.watchlist.dto.WatchlistItemResponse;

@WebMvcTest(WatchlistController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WatchlistService watchlistService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedWatchlistRequest() throws Exception {
        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsCurrentUsersWatchlist() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(watchlistService.getWatchlist(currentUser)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/watchlist").with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"));
    }

    @Test
    void addsStockForAuthenticatedUser() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);
        when(watchlistService.addToWatchlist("AAPL", currentUser))
                .thenReturn(response());

        mockMvc.perform(post("/api/watchlist")
                        .with(user("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"AAPL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.symbol").value("AAPL"));
    }

    @Test
    void removesStockForAuthenticatedUser() throws Exception {
        User currentUser = new User("Demo User", "demo@example.com", "hash");
        when(currentUserService.requireUser("demo@example.com")).thenReturn(currentUser);

        mockMvc.perform(delete("/api/watchlist/AAPL")
                        .with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private WatchlistItemResponse response() {
        return new WatchlistItemResponse(
                UUID.randomUUID(),
                "AAPL",
                "Apple Inc.",
                "NASDAQ",
                "USD",
                new BigDecimal("195.25"),
                new BigDecimal("1.45"),
                new BigDecimal("0.75"),
                Instant.parse("2026-06-24T16:00:00Z"),
                Instant.parse("2026-06-24T10:00:00Z"));
    }
}

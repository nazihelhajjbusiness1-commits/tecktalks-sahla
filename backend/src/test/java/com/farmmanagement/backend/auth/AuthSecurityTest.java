package com.farmmanagement.backend.auth;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DT-84 + security hardening: verifies the refresh-token flow, logout
 * invalidation, and the 429 brute-force lockout behaviour.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    private JsonNode login(String email, String password) throws Exception {
        String body = mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("data");
    }

    @Test
    void login_issuesAccessAndRefreshTokens() throws Exception {
        JsonNode data = login("admin@sahla.lb", "Password123");
        org.junit.jupiter.api.Assertions.assertFalse(data.get("token").asText().isEmpty());
        org.junit.jupiter.api.Assertions.assertFalse(data.get("refreshToken").asText().isEmpty());
    }

    @Test
    void refreshToken_issuesANewAccessToken() throws Exception {
        String refreshToken = login("manager@sahla.lb", "Password123").get("refreshToken").asText();

        mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void accessToken_isRejected_afterLogout() throws Exception {
        String accessToken = login("manager@sahla.lb", "Password123").get("token").asText();

        // Token works before logout.
        mockMvc().perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc().perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Same token is now revoked -> unauthenticated -> 401 on a protected read.
        mockMvc().perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailedLogins_areLockedOutWith429() throws Exception {
        String email = "lockout-probe@sahla.lb"; // unknown account; its own rate-limit key

        for (int i = 0; i < 4; i++) {
            mockMvc().perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"nope\"}"))
                    .andExpect(status().isUnauthorized());
        }

        // The 5th failure crosses the threshold and locks the account.
        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"nope\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}

package com.farmmanagement.backend.dashboard;

import com.farmmanagement.backend.TestcontainersConfiguration;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DT-82/83/84/85: dashboard + report endpoints enforce the role matrix (one
 * allowed + one forbidden role each) and return valid empty states when there
 * is no data.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DashboardAndReportPermissionTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    private String token(String email) throws Exception {
        String body = mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("data").get("token").asText();
    }

    @Test
    void manager_canReadDashboard_withValidEmptyState() throws Exception {
        mockMvc().perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token("manager@sahla.lb")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todaysDeliveries").exists())
                .andExpect(jsonPath("$.amountOwedToFarmers").isArray())
                .andExpect(jsonPath("$.recentDeliveries").isArray());
    }

    @Test
    void inspector_isForbiddenFromDashboard() throws Exception {
        mockMvc().perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token("inspector@sahla.lb")))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_canReadOutstandingPaymentsReport() throws Exception {
        mockMvc().perform(get("/api/reports/outstanding-payments")
                        .header("Authorization", "Bearer " + token("manager@sahla.lb")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dailyCollectionReport_rejectsInvalidDateRange() throws Exception {
        mockMvc().perform(get("/api/reports/daily-collection")
                        .param("from", "2026-10-10")
                        .param("to", "2026-10-01")
                        .header("Authorization", "Bearer " + token("manager@sahla.lb")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inspector_isForbiddenFromOutstandingPaymentsReport() throws Exception {
        mockMvc().perform(get("/api/reports/outstanding-payments")
                        .header("Authorization", "Bearer " + token("inspector@sahla.lb")))
                .andExpect(status().isForbidden());
    }
}

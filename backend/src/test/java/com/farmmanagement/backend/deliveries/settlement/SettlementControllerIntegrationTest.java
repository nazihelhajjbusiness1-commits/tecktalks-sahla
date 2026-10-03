package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-layer integration tests for the Sprint 4 settlement endpoints (DT-79).
 * Ahmad's service-level tests (PriceResolutionServiceTest, SettlementCalculationServiceTest,
 * SettlementConfirmationServiceTest) already cover the calculation formulas and service-level
 * idempotency against a real PostgreSQL container via {@link TestcontainersConfiguration}.
 * This class instead drives the full controller/security stack with MockMvc, so role-based
 * access control (DT-69's "only authorized roles may confirm") and HTTP status/error-body
 * contracts are actually exercised end to end.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SettlementControllerIntegrationTest {

    private static final String MANAGER_EMAIL = "manager@sahla.lb";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void previewSettlement_forConfirmedDelivery_resolvesUsdPrice() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.unitPrice").value(1.20))
                .andExpect(jsonPath("$.acceptedWeight").value(500.000));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void previewSettlement_forNonConfirmedDelivery_returns409() throws Exception {
        long farmerId = createFarmer();
        long productId = createProduct("Apple", "Preview-Pending");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);
        createPrice(productId, gradeId, "1.20", "USD");
        long deliveryId = createDelivery(farmerId, productId, "500.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/preview"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void calculateSettlement_withCommissionAndDeduction_returnsExpectedAmounts() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(calculationRequest("20.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grossAmount").value(600.00))
                .andExpect(jsonPath("$.commissionAmount").value(30.00))
                .andExpect(jsonPath("$.deductionsTotal").value(20.00))
                .andExpect(jsonPath("$.netAmount").value(550.00))
                .andExpect(jsonPath("$.status").value("CALCULATED"));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void calculateSettlement_withNegativeDeductionAmount_returns400() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(calculationRequest("-20.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void calculateSettlement_asUnauthorizedRole_returns403() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(calculationRequest("20.00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void calculateSettlement_withoutAuthentication_returns401() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(calculationRequest("20.00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void confirmSettlement_freezesAmountsAndRecordsActor() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");
        calculate(deliveryId, "20.00");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.netAmount").value(550.00))
                .andExpect(jsonPath("$.confirmedAt").isNotEmpty())
                .andExpect(jsonPath("$.confirmedBy").value("Sahla Manager"));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void confirmSettlement_repeated_returnsSameSettlementWithoutDuplicate() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");
        calculate(deliveryId, "20.00");

        String first = mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/confirm"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long firstSettlementId = ((Number) JsonPath.read(first, "$.id")).longValue();

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstSettlementId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void confirmSettlement_asUnauthorizedRole_returns403() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void getSettlement_afterConfirmation_returnsNetAmount() throws Exception {
        long deliveryId = createConfirmedDelivery("500.000", "520.000", "20.000");
        calculate(deliveryId, "20.00");
        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/confirm"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deliveries/" + deliveryId + "/settlement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netAmount").value(550.00));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, roles = "MANAGER")
    void getSettlement_whenNoneExists_returns404() throws Exception {
        long farmerId = createFarmer();
        long productId = createProduct("Apple", "Get-NotFound");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);
        createPrice(productId, gradeId, "1.20", "USD");
        long deliveryId = createDelivery(farmerId, productId, "500.000");

        mockMvc.perform(get("/api/deliveries/" + deliveryId + "/settlement"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ================= Helpers =================

    private void calculate(long deliveryId, String deductionAmount) throws Exception {
        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/settlement/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(calculationRequest(deductionAmount)))
                .andExpect(status().isOk());
    }

    private String calculationRequest(String deductionAmount) {
        return """
                {
                  "currency": "USD",
                  "commissionRate": 5.00,
                  "deductions": [
                    { "type": "TRANSPORT", "description": "Transport fee", "amount": %s }
                  ]
                }
                """.formatted(deductionAmount);
    }

    /**
     * Builds a confirmed 500kg Grade A delivery priced at $1.20/kg (the Sprint 4 demo scenario),
     * ready for settlement preview/calculate/confirm.
     */
    private long createConfirmedDelivery(String quantity, String grossWeight, String tareWeight) throws Exception {
        long farmerId = createFarmer();
        long productId = createProduct("Apple", "Settlement-IT-" + System.nanoTime());
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);
        createPrice(productId, gradeId, "1.20", "USD");
        long deliveryId = createDelivery(farmerId, productId, quantity);
        captureWeight(deliveryId, grossWeight, tareWeight);
        gradeDelivery(deliveryId, "GRADE_A");
        return deliveryId;
    }

    private long createFarmer() throws Exception {
        String response = mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Settlement Farmer",
                                  "phone": "70123456",
                                  "village": "Bcharre"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long createProduct(String name, String variety) throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "variety": "%s",
                                  "unit": "KG"
                                }
                                """.formatted(name, variety)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long createGrade(long productId, String code, String name, int order) throws Exception {
        String response = mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "%s",
                                  "name": "%s",
                                  "description": "seed",
                                  "displayOrder": %d,
                                  "active": true
                                }
                                """.formatted(code, name, order)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void createPrice(long productId, long gradeId, String amount, String currency) throws Exception {
        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": %s,
                                  "currency": "%s",
                                  "effectiveFrom": "2026-01-01T00:00:00Z",
                                  "active": true
                                }
                                """.formatted(gradeId, amount, currency)))
                .andExpect(status().isCreated());
    }

    private long createDelivery(long farmerId, long productId, String quantity) throws Exception {
        String response = mockMvc.perform(post("/api/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmerId": %d,
                                  "productId": %d,
                                  "quantity": %s,
                                  "deliveryDate": "2026-09-01",
                                  "notes": "Settlement integration test"
                                }
                                """.formatted(farmerId, productId, quantity)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void captureWeight(long deliveryId, String grossWeight, String tareWeight) throws Exception {
        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "grossWeight": %s,
                                  "tareWeight": %s
                                }
                                """.formatted(grossWeight, tareWeight)))
                .andExpect(status().isCreated());
    }

    private void gradeDelivery(long deliveryId, String grade) throws Exception {
        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "grade": "%s"
                                }
                                """.formatted(grade)))
                .andExpect(status().isCreated());
    }
}

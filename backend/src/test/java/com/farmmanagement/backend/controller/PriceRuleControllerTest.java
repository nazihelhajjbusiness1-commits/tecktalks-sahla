package com.farmmanagement.backend.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Same security gap as GradeDefinitionControllerTest: SecurityConfig does
 * not restrict /api/products/{id}/prices or /api/prices/{id} by role, so
 * there is no genuine "wrong role" 403 case here - only unauthenticated
 * access is covered.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PriceRuleControllerTest {

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

    // ---- create: happy path ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_inUsd_returns201() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 1.20,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.gradeId").value(gradeId))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(1.20));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_inLbp_returns201() throws Exception {
        Long productId = createProduct("Tomato", "Baladi");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 85000,
                                  "currency": "LBP",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("LBP"));
    }

    // ---- create: validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_withNegativeAmount_returns400() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": -1.00,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Price must be zero or greater"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_withoutCurrency_returns400() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 1.20,
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Currency is required"));
    }

    // ---- create: grade must belong to the selected product ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_withGradeFromAnotherProduct_returns400() throws Exception {
        Long appleId = createProduct("Apple", "Lebanese Golden");
        Long tomatoId = createProduct("Tomato", "Baladi");
        Long tomatoGradeId = createGrade(tomatoId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + appleId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 1.20,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """.formatted(tomatoGradeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Grade with id " + tomatoGradeId + " does not belong to product with id " + appleId));
    }

    // ---- create: invalid product reference ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_forUnknownProduct_returns404() throws Exception {
        mockMvc.perform(post("/api/products/999999999/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": 1,
                                  "amount": 1.20,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999999999"));
    }

    // ---- create: conflicting effective price periods ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_withOverlappingActivePeriod_returns400() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);
        createPrice(productId, gradeId, "1.20", "USD", "2026-09-01T00:00:00Z", null);

        // Same product/grade/currency, no end date on the existing rule - any
        // later start date overlaps it.
        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 1.35,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-10-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Active price period overlaps with an existing rule for this product, grade, and currency."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPrice_withNonOverlappingPeriodAfterExistingEnds_isAllowed() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);
        createPrice(productId, gradeId, "1.20", "USD", "2026-09-01T00:00:00Z", "2026-10-01T00:00:00Z");

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": 1.35,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-10-01T00:00:00Z"
                                }
                                """.formatted(gradeId)))
                .andExpect(status().isCreated());
    }

    // ---- create: auth ----

    @Test
    void createPrice_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/products/1/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": 1,
                                  "amount": 1.20,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ---- list ----

    @Test
    @WithMockUser(roles = "WAREHOUSE_EMPLOYEE")
    void getPricesForProduct_returnsAllPrices() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);
        createPrice(productId, gradeId, "1.20", "USD", "2026-09-01T00:00:00Z", null);

        mockMvc.perform(get("/api/products/" + productId + "/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId));
    }

    // ---- update ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePrice_withValidData_returns200() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);
        Long priceId = createPrice(productId, gradeId, "1.20", "USD", "2026-09-01T00:00:00Z", null);

        mockMvc.perform(put("/api/prices/" + priceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 1.35,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1.35));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePrice_withUnknownId_returns404() throws Exception {
        mockMvc.perform(put("/api/prices/999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 1.35,
                                  "currency": "USD",
                                  "effectiveFrom": "2026-09-01T00:00:00Z",
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("PriceRule not found with id: 999999999"));
    }

    // ---- helpers ----

    // The helpers below always authenticate as ADMIN regardless of the
    // calling test's own @WithMockUser role, since they're just fixture
    // setup (product/grade/price creation), not the thing under test.

    private Long createProduct(String name, String variety) throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .with(user("fixture-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "variety": "%s",
                                  "unit": "KG"
                                }
                                """.formatted(name, variety)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private Long createGrade(Long productId, String gradeCode, String name, int displayOrder) throws Exception {
        String response = mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .with(user("fixture-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "%s",
                                  "name": "%s",
                                  "displayOrder": %d
                                }
                                """.formatted(gradeCode, name, displayOrder)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private Long createPrice(
            Long productId,
            Long gradeId,
            String amount,
            String currency,
            String effectiveFrom,
            String effectiveTo
    ) throws Exception {
        String effectiveToJson = effectiveTo == null ? "null" : "\"" + effectiveTo + "\"";

        String response = mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .with(user("fixture-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeId": %d,
                                  "amount": %s,
                                  "currency": "%s",
                                  "effectiveFrom": "%s",
                                  "effectiveTo": %s
                                }
                                """.formatted(gradeId, amount, currency, effectiveFrom, effectiveToJson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }
}

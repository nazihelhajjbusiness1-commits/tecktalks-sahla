package com.farmmanagement.backend.grading;

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
 * Integration tests for grade definitions (DT-38) and grade-based pricing (DT-39),
 * covering persistence, REST behaviour, validation and role-based security.
 * Uses a real PostgreSQL container via {@link TestcontainersConfiguration}.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class GradeAndPricingIntegrationTest {

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

    // ================= Grades =================

    @Test
    @WithMockUser(roles = "MANAGER")
    void createGrade_forExistingProduct_returns201() throws Exception {
        long productId = createProduct("Apple", "Test Red");

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A", "Grade A", 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeCode").value("GRADE_A"))
                .andExpect(jsonPath("$.productId").value((int) productId));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getGrades_returnsInDisplayOrder() throws Exception {
        long productId = createProduct("Apple", "Ordered");
        createGrade(productId, "GRADE_B", "Grade B", 1);
        createGrade(productId, "GRADE_A", "Grade A", 0);

        mockMvc.perform(get("/api/products/" + productId + "/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gradeCode").value("GRADE_A"))
                .andExpect(jsonPath("$[1].gradeCode").value("GRADE_B"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createGrade_withDuplicateCodeForSameProduct_returns409() throws Exception {
        long productId = createProduct("Apple", "Dup");
        createGrade(productId, "GRADE_A", "Grade A", 0);

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A", "Grade A again", 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createGrade_forUnknownProduct_returns404() throws Exception {
        mockMvc.perform(post("/api/products/999999/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A", "Grade A", 0)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ================= Pricing =================

    @Test
    @WithMockUser(roles = "MANAGER")
    void createPrice_inUsd_returns201() throws Exception {
        long productId = createProduct("Apple", "Priced USD");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(price(gradeId, "1.20", "USD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(1.20));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createPrice_inLbp_returns201() throws Exception {
        long productId = createProduct("Tomato", "Priced LBP");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(price(gradeId, "90000.00", "LBP")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("LBP"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createPrice_withNegativeAmount_returns400() throws Exception {
        long productId = createProduct("Apple", "Neg");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);

        mockMvc.perform(post("/api/products/" + productId + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(price(gradeId, "-1.00", "USD")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createPrice_forGradeNotBelongingToProduct_returns400() throws Exception {
        long productA = createProduct("Apple", "Owner A");
        long productB = createProduct("Apple", "Owner B");
        long gradeOfA = createGrade(productA, "GRADE_A", "Grade A", 0);

        // Try to price productB using a grade that belongs to productA.
        mockMvc.perform(post("/api/products/" + productB + "/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(price(gradeOfA, "1.00", "USD")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ================= Security =================

    @Test
    void createGrade_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/products/1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A", "Grade A", 0)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void createGrade_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/products/1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A", "Grade A", 0)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void createPrice_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/products/1/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(price(1L, "1.00", "USD")))
                .andExpect(status().isForbidden());
    }

    // ================= Helpers =================

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
                        .content(grade(code, name, order)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private String grade(String code, String name, int order) {
        return """
                {
                  "gradeCode": "%s",
                  "name": "%s",
                  "description": "seed",
                  "displayOrder": %d,
                  "active": true
                }
                """.formatted(code, name, order);
    }

    private String price(long gradeId, String amount, String currency) {
        return """
                {
                  "gradeId": %d,
                  "amount": %s,
                  "currency": "%s",
                  "effectiveFrom": "2026-01-01T00:00:00Z",
                  "active": true
                }
                """.formatted(gradeId, amount, currency);
    }
}

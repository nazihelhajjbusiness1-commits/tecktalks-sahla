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
 * SecurityConfig does not restrict /api/products/{id}/grades or
 * /api/grades/{id} by role (unlike Farmers/Products) - any authenticated
 * user can create/update grades today. So there is no genuine "wrong role"
 * 403 case to test here; only unauthenticated access is covered for the
 * security dimension. Flagged separately as a gap to confirm with the team.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class GradeDefinitionControllerTest {

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
    void createGrade_withValidData_returns201() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Grade A - Premium",
                                  "description": "Top quality, minimal blemishes",
                                  "displayOrder": 1,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.gradeCode").value("A"))
                .andExpect(jsonPath("$.displayOrder").value(1));
    }

    // ---- create: validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGrade_withBlankGradeCode_returns400() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Grade A - Premium",
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Grade code is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGrade_withNegativeDisplayOrder_returns400() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Grade A - Premium",
                                  "displayOrder": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Display order must be non-negative"));
    }

    // ---- create: invalid product reference ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGrade_forUnknownProduct_returns404() throws Exception {
        mockMvc.perform(post("/api/products/999999999/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Grade A - Premium",
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999999999"));
    }

    // ---- create: duplicate grade code within the same product ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGrade_withDuplicateCodeForSameProduct_returns409() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + productId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Duplicate Grade A",
                                  "displayOrder": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Grade code 'A' already exists for product ID: " + productId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGrade_withSameCodeForDifferentProduct_isAllowed() throws Exception {
        // Grade codes are only unique per product, not globally.
        Long appleId = createProduct("Apple", "Lebanese Golden");
        Long tomatoId = createProduct("Tomato", "Baladi");
        createGrade(appleId, "A", "Grade A - Premium", 1);

        mockMvc.perform(post("/api/products/" + tomatoId + "/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Grade A - Premium",
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // ---- create: auth ----

    @Test
    void createGrade_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/products/1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gradeCode": "A",
                                  "name": "Grade A - Premium",
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ---- list ----

    @Test
    @WithMockUser(roles = "WAREHOUSE_EMPLOYEE")
    void getGradesForProduct_returnsGradesInDisplayOrder() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        createGrade(productId, "B", "Grade B - Standard", 2);
        createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(get("/api/products/" + productId + "/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gradeCode").value("A"))
                .andExpect(jsonPath("$[1].gradeCode").value("B"));
    }

    // ---- update ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGrade_withValidData_returns200() throws Exception {
        Long productId = createProduct("Apple", "Lebanese Golden");
        Long gradeId = createGrade(productId, "A", "Grade A - Premium", 1);

        mockMvc.perform(put("/api/grades/" + gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Grade A - Premium (Revised)",
                                  "description": "Updated description",
                                  "displayOrder": 1,
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grade A - Premium (Revised)"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGrade_withUnknownId_returns404() throws Exception {
        mockMvc.perform(put("/api/grades/999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Grade A - Premium",
                                  "displayOrder": 1,
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("GradeDefinition not found with id: 999999999"));
    }

    // ---- helpers ----

    private Long createProduct(String name, String variety) throws Exception {
        // Always authenticated as ADMIN regardless of the calling test's own
        // @WithMockUser role, since product creation is role-restricted and
        // this is just fixture setup, not the thing under test.
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
}

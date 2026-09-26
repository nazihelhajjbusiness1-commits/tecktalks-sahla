package com.farmmanagement.backend.deliveries;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for delivery creation/retrieval/lifecycle (DT-51), weight
 * capture (DT-52), and manual grading & confirmation (DT-53).
 * Uses a real PostgreSQL container via {@link TestcontainersConfiguration}; the
 * demo users seeded by V8__seed_demo_users.sql (admin@sahla.lb etc.) run
 * against it, which DeliveryController/DeliveryWeightController/
 * DeliveryGradeController all depend on to resolve the acting user.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
@WithMockUser(username = "admin@sahla.lb", roles = "ADMIN")
class DeliveryLifecycleIntegrationTest {

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

    // ================= Create / Retrieve =================

    @Test
    void createDelivery_withValidData_returns200() throws Exception {
        long farmerId = createFarmer("Youssef Haddad");
        long productId = createProduct("Apple", "Create Valid");

        mockMvc.perform(post("/api/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(delivery(farmerId, productId, "380.0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.quantity").value(380.0));
    }

    @Test
    void createDelivery_missingFarmerId_returns400() throws Exception {
        long productId = createProduct("Apple", "Missing Farmer");

        mockMvc.perform(post("/api/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 380.0,
                                  "deliveryDate": "2026-09-23"
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void createDelivery_unknownFarmer_returns404() throws Exception {
        long productId = createProduct("Apple", "Unknown Farmer");

        mockMvc.perform(post("/api/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(delivery(999999999L, productId, "380.0")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDelivery_withoutAuthentication_returns401() throws Exception {
        long farmerId = createFarmer("No Auth Farmer");
        long productId = createProduct("Apple", "No Auth");

        mockMvc.perform(post("/api/deliveries")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(delivery(farmerId, productId, "380.0")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createDelivery_withWrongRole_returns403() throws Exception {
        long farmerId = createFarmer("Wrong Role Farmer");
        long productId = createProduct("Apple", "Wrong Role");

        mockMvc.perform(post("/api/deliveries")
                        .with(user("inspector@sahla.lb").roles("INSPECTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(delivery(farmerId, productId, "380.0")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDelivery_returnsCreatedDelivery() throws Exception {
        long farmerId = createFarmer("Get Valid Farmer");
        long productId = createProduct("Apple", "Get Valid");
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(get("/api/deliveries/" + deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) deliveryId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getDelivery_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/deliveries/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDeliveries_filtersByStatusAndFarmer() throws Exception {
        long farmerA = createFarmer("List Farmer A");
        long farmerB = createFarmer("List Farmer B");
        long productId = createProduct("Apple", "List Filter");
        long deliveryA = createDelivery(farmerA, productId, "100.0");
        createDelivery(farmerB, productId, "200.0");

        mockMvc.perform(get("/api/deliveries")
                        .param("status", "PENDING")
                        .param("farmerId", String.valueOf(farmerA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value((int) deliveryA));
    }

    @Test
    void listDeliveries_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/api/deliveries")
                        .param("from", "2026-09-30")
                        .param("to", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDelivery_updatesNotes_returns200() throws Exception {
        long farmerId = createFarmer("Update Farmer");
        long productId = createProduct("Apple", "Update Valid");
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(put("/api/deliveries/" + deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notes": "Corrected note after re-weigh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Corrected note after re-weigh"));
    }

    @Test
    void updateDelivery_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/deliveries/999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notes": "n/a"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // ================= Weight Capture =================

    @Test
    void captureWeight_withMatchingNetWeight_transitionsToWeighed() throws Exception {
        long farmerId = createFarmer("Weight Valid Farmer");
        long productId = createProduct("Apple", "Weight Valid");
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight("500.0", "120.0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.netWeight").value(380.0));

        mockMvc.perform(get("/api/deliveries/" + deliveryId))
                .andExpect(jsonPath("$.status").value("WEIGHED"));
    }

    @Test
    void captureWeight_tareNotLessThanGross_returns400() throws Exception {
        long farmerId = createFarmer("Weight Tare Farmer");
        long productId = createProduct("Apple", "Weight Tare");
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight("100.0", "150.0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void captureWeight_netWeightMismatchesQuantity_returns400() throws Exception {
        long farmerId = createFarmer("Weight Mismatch Farmer");
        long productId = createProduct("Apple", "Weight Mismatch");
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight("500.0", "50.0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void captureWeight_onAlreadyWeighedDelivery_returns409() throws Exception {
        long farmerId = createFarmer("Weight Conflict Farmer");
        long productId = createProduct("Apple", "Weight Conflict");
        long deliveryId = createDelivery(farmerId, productId, "380.0");
        captureWeight(deliveryId, "500.0", "120.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight("500.0", "120.0")))
                .andExpect(status().isConflict());
    }

    @Test
    void captureWeight_deliveryNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/deliveries/999999999/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight("500.0", "120.0")))
                .andExpect(status().isNotFound());
    }

    // ================= Manual Grading & Confirmation =================

    @Test
    void gradeDelivery_valid_confirmsAndComputesPrice() throws Exception {
        long farmerId = createFarmer("Grade Valid Farmer");
        long productId = createProduct("Apple", "Grade Valid");
        long gradeId = createGrade(productId, "GRADE_A", "Grade A", 0);
        createPriceRule(productId, gradeId, "1.50", "USD");
        long deliveryId = createDelivery(farmerId, productId, "380.0");
        captureWeight(deliveryId, "500.0", "120.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultingStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(570.0));

        mockMvc.perform(get("/api/deliveries/" + deliveryId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(570.0));
    }

    @Test
    void gradeDelivery_reject_confirmsWithoutPrice() throws Exception {
        long farmerId = createFarmer("Grade Reject Farmer");
        long productId = createProduct("Apple", "Grade Reject");
        createGrade(productId, "REJECT", "Reject", 0);
        long deliveryId = createDelivery(farmerId, productId, "380.0");
        captureWeight(deliveryId, "500.0", "120.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("REJECT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultingStatus").value("REJECTED"))
                .andExpect(jsonPath("$.totalPrice").doesNotExist());
    }

    @Test
    void gradeDelivery_notYetWeighed_returns409() throws Exception {
        long farmerId = createFarmer("Grade Not Weighed Farmer");
        long productId = createProduct("Apple", "Grade Not Weighed");
        createGrade(productId, "GRADE_A", "Grade A", 0);
        long deliveryId = createDelivery(farmerId, productId, "380.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A")))
                .andExpect(status().isConflict());
    }

    @Test
    void gradeDelivery_unknownGradeForProduct_returns404() throws Exception {
        long farmerId = createFarmer("Grade Unknown Farmer");
        long productId = createProduct("Apple", "Grade Unknown");
        long deliveryId = createDelivery(farmerId, productId, "380.0");
        captureWeight(deliveryId, "500.0", "120.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("PREMIUM")))
                .andExpect(status().isNotFound());
    }

    @Test
    void gradeDelivery_withWrongRole_returns403() throws Exception {
        long farmerId = createFarmer("Grade Wrong Role Farmer");
        long productId = createProduct("Apple", "Grade Wrong Role");
        createGrade(productId, "GRADE_A", "Grade A", 0);
        long deliveryId = createDelivery(farmerId, productId, "380.0");
        captureWeight(deliveryId, "500.0", "120.0");

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/grade")
                        .with(user("receiving@sahla.lb").roles("RECEIVING_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade("GRADE_A")))
                .andExpect(status().isForbidden());
    }

    // ================= Helpers =================

    private long createFarmer(String name) throws Exception {
        String response = mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """.formatted(name)))
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

    private void createPriceRule(long productId, long gradeId, String amount, String currency) throws Exception {
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
                        .content(delivery(farmerId, productId, quantity)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void captureWeight(long deliveryId, String gross, String tare) throws Exception {
        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weight(gross, tare)))
                .andExpect(status().isCreated());
    }

    private String delivery(long farmerId, long productId, String quantity) {
        return """
                {
                  "farmerId": %d,
                  "productId": %d,
                  "quantity": %s,
                  "deliveryDate": "2026-09-23"
                }
                """.formatted(farmerId, productId, quantity);
    }

    private String weight(String gross, String tare) {
        return """
                {
                  "grossWeight": %s,
                  "tareWeight": %s
                }
                """.formatted(gross, tare);
    }

    private String grade(String code) {
        return """
                {
                  "grade": "%s"
                }
                """.formatted(code);
    }
}

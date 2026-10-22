package com.farmmanagement.backend.farmers;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class FarmerControllerTest {

    private static final String BASE_URL = "/api/farmers";

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
    void createFarmer_withValidData_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ahmad Farms"))
                .andExpect(jsonPath("$.phone").value("70123456"))
                .andExpect(jsonPath("$.village").value("Aley"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.farmerCode").isNotEmpty());
    }

    // ---- create: validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFarmer_withBlankName_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Farmer name is required"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFarmer_withBlankPhone_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone number is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFarmer_withBlankVillage_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Village is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFarmer_withInvalidPhoneFormat_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "not-a-phone-number",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Lebanese phone number"));
    }

    // ---- create: auth ----

    @Test
    void createFarmer_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void createFarmer_withRoleNotAllowedToCreate_returns403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // ---- create: duplicate farmer code ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFarmer_withDuplicateFarmerCode_returns409() throws Exception {
        String body = """
                {
                  "farmerCode": "F-DUP-1",
                  "name": "First Farm",
                  "phone": "70123456",
                  "village": "Aley"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmerCode").value("F-DUP-1"));

        // Same code again -> conflict, same error shape as other endpoints.
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmerCode": "F-DUP-1",
                                  "name": "Second Farm",
                                  "phone": "70123457",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    // ---- get ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFarmer_withUnknownId_returns404WithReadableMessage() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Farmer not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFarmers_withSearch_returnsMatchingFarmer() throws Exception {
        createFarmer("Searchable Farm", "70123456", "Aley");

        mockMvc.perform(get(BASE_URL).param("search", "Searchable Farm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Searchable Farm"));
    }

    // ---- update: not found / validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFarmer_withUnknownId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Farmer not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFarmer_withInvalidStatus_returns400WithReadableMessage() throws Exception {
        Long farmerId = createFarmer("Ahmad Farms", "70123456", "Aley");

        mockMvc.perform(put(BASE_URL + "/" + farmerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley",
                                  "status": "SUSPENDED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status must be either ACTIVE or INACTIVE"));
    }

    // ---- update: status activate/deactivate (the feature this suite exists to pin down) ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFarmer_withStatusInactive_deactivatesFarmer() throws Exception {
        Long farmerId = createFarmer("Ahmad Farms", "70123456", "Aley");

        mockMvc.perform(put(BASE_URL + "/" + farmerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFarmer_withStatusOmitted_leavesExistingStatusUnchanged() throws Exception {
        Long farmerId = createFarmer("Ahmad Farms", "70123456", "Aley");

        // Deactivate first.
        mockMvc.perform(put(BASE_URL + "/" + farmerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk());

        // Update other fields without sending "status" at all - it should stay INACTIVE.
        mockMvc.perform(put(BASE_URL + "/" + farmerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms Updated",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ahmad Farms Updated"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "RECEIVING_EMPLOYEE")
    void updateFarmer_withRoleNotAllowedToUpdate_returns403() throws Exception {
        // RECEIVING_EMPLOYEE can create/read farmers but not update them.
        Long farmerId = createFarmer("Ahmad Farms", "70123456", "Aley");

        mockMvc.perform(put(BASE_URL + "/" + farmerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ahmad Farms",
                                  "phone": "70123456",
                                  "village": "Aley"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // ---- helper ----

    private Long createFarmer(String name, String phone, String village) throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "phone": "%s",
                                  "village": "%s"
                                }
                                """.formatted(name, phone, village)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }
}

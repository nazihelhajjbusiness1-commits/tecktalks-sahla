package com.farmmanagement.backend.products;

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
class ProductControllerTest {

    private static final String BASE_URL = "/api/products";

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
    void createProduct_withValidData_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "BOX"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tomato"))
                .andExpect(jsonPath("$.variety").value("Roma"))
                .andExpect(jsonPath("$.unit").value("BOX"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withUnitOmitted_defaultsToKg() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Cherry"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unit").value("KG"));
    }

    // ---- create: validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withBlankName_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "variety": "Roma",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Product name is required"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withBlankVariety_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product variety is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withInvalidUnit_returns400WithReadableMessage() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "LITER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unit must be one of: KG, TON, BOX, CRATE"));
    }

    // ---- create: auth ----

    @Test
    void createProduct_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(roles = "RECEIVING_EMPLOYEE")
    void createProduct_withRoleNotAllowedToCreate_returns403() throws Exception {
        // RECEIVING_EMPLOYEE can read products but not create them.
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // ---- create: duplicate name+variety ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withDuplicateNameAndVariety_returns409() throws Exception {
        createProduct("Tomato", "Roma", "KG");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "BOX"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A product with this name and variety already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withDuplicateNameDifferentCase_returns409() throws Exception {
        // "Tomato"/"Roma" and "tomato"/"roma" must be treated as the same combination.
        createProduct("Tomato", "Roma", "KG");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "tomato",
                                  "variety": "roma",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withSameNameDifferentVariety_isAllowed() throws Exception {
        // The whole point of this rule: same name is fine as long as the
        // variety differs - only the (name, variety) combination is unique.
        createProduct("Tomato", "Roma", "KG");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Cherry",
                                  "unit": "KG"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tomato"))
                .andExpect(jsonPath("$.variety").value("Cherry"));
    }

    // ---- get ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProduct_withUnknownId_returns404WithReadableMessage() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    @WithMockUser(roles = "INSPECTOR")
    void getProducts_withSearch_isAllowedForEveryRole() throws Exception {
        // GET is open to all six roles - INSPECTOR here just proves it's
        // not restricted to ADMIN/MANAGER the way create/update are.
        mockMvc.perform(get(BASE_URL).param("search", "Tomato"))
                .andExpect(status().isOk());
    }

    // ---- update: not found / validation ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_withUnknownId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG",
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // ---- update: duplicate name+variety ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_toMatchAnotherProduct_returns409() throws Exception {
        createProduct("Tomato", "Roma", "KG");
        Long cherryId = createProduct("Tomato", "Cherry", "KG");

        // Try to rename the Cherry product into a duplicate of the Roma one.
        mockMvc.perform(put(BASE_URL + "/" + cherryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG",
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A product with this name and variety already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_withoutChangingNameOrVariety_doesNotConflictWithItself() throws Exception {
        Long productId = createProduct("Tomato", "Roma", "KG");

        // Same name/variety as its own current values - must not be treated
        // as a duplicate of itself. Only the active flag changes here.
        mockMvc.perform(put(BASE_URL + "/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG",
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE_EMPLOYEE")
    void updateProduct_withRoleNotAllowedToUpdate_returns403() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tomato",
                                  "variety": "Roma",
                                  "unit": "KG",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // ---- helper ----

    private Long createProduct(String name, String variety, String unit) throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "variety": "%s",
                                  "unit": "%s"
                                }
                                """.formatted(name, variety, unit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }
}

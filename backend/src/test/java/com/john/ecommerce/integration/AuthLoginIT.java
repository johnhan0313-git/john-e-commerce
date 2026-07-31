package com.john.ecommerce.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.support.AbstractIntegrationTest;
import com.john.ecommerce.support.TestAuthHelper;
import com.john.ecommerce.support.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder.ensureSchemaPatches();
    }

    @Test
    void loginReturnsJwtAndTenantScopedUser() throws Exception {
        String bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper);
        assertThat(bearer).startsWith("Bearer ");

        mockMvc.perform(get("/tenant/modules").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void mallPortalAutoRegistersUnknownEmail() throws Exception {
        String email = "buyer-auto-" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/auth/email-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","portal":"mall"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .header("X-Tenant-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s","portal":"mall"}
                                """.formatted(email, TestAuthHelper.DEMO_CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.userType").value(0))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.path("data").path("user").path("id").asLong()).isPositive();
    }

    @Test
    void adminPortalRejectsUnknownEmail() throws Exception {
        String email = "admin-unknown-" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/auth/email-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","portal":"admin"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}

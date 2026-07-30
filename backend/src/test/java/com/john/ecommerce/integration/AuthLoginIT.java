package com.john.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.support.AbstractIntegrationTest;
import com.john.ecommerce.support.TestAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginReturnsJwtAndTenantScopedUser() throws Exception {
        // Ensure local schema patches even for smoke login
        // (AuthLoginIT does not call TestDataSeeder by default)
        String bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper);
        assertThat(bearer).startsWith("Bearer ");

        mockMvc.perform(get("/tenant/modules").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

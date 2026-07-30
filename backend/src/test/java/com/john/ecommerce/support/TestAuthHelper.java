package com.john.ecommerce.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestAuthHelper {

    public static final String DEMO_PHONE = "13800000000";
    public static final String DEMO_PASSWORD = "admin123";

    private TestAuthHelper() {}

    public static String loginAndBearer(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        return loginAndBearer(mockMvc, objectMapper, DEMO_PHONE, DEMO_PASSWORD);
    }

    public static String loginAndBearer(MockMvc mockMvc, ObjectMapper objectMapper,
                                        String phone, String password) throws Exception {
        String body = """
                {"phone":"%s","password":"%s"}
                """.formatted(phone, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + root.path("data").path("token").asText();
    }
}

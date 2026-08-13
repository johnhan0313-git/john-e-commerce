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

    public static final String DEMO_EMAIL = "johnhan0313@gmail.com";
    /** Matches app.auth.fixed-code in application-test.yml */
    public static final String DEMO_CODE = "123456";

    private TestAuthHelper() {}

    public static String loginAndBearer(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        return loginAndBearer(mockMvc, objectMapper, DEMO_EMAIL, DEMO_CODE);
    }

    public static String loginAndBearer(MockMvc mockMvc, ObjectMapper objectMapper,
                                        String email, String code) throws Exception {
        return loginAndBearer(mockMvc, objectMapper, email, code, "mall");
    }

    public static String loginAndBearer(MockMvc mockMvc, ObjectMapper objectMapper,
                                        String email, String code, String portal) throws Exception {
        mockMvc.perform(post("/auth/email-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"portal\":\"" + portal + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String body = """
                {"email":"%s","code":"%s","portal":"%s"}
                """.formatted(email, code, portal);
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

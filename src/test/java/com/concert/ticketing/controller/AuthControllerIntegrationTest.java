package com.concert.ticketing.controller;

import com.concert.ticketing.AbstractIntegrationTest;
import com.concert.ticketing.dto.auth.LoginRequest;
import com.concert.ticketing.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void shouldRegisterAndLoginUser() throws Exception {
                RegisterRequest registerRequest = new RegisterRequest("testuser", "password123", "User Test",
                                "test@example.com", "USER");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("Success")));

                mockMvc.perform(post("/api/v1/auth/login")
                                .header("Authorization",
                                                "Basic " + java.util.Base64.getEncoder()
                                                                .encodeToString("testuser:password123".getBytes())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("Success")))
                                .andExpect(jsonPath("$.token", notNullValue()));
        }

        @Test
        public void shouldFailLoginWithWrongPassword() throws Exception {
                RegisterRequest registerRequest = new RegisterRequest("wrongpassuser", "password123", "Wrong Pass",
                                "wrong@example.com", "USER");
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/v1/auth/login")
                                .header("Authorization",
                                                "Basic " + java.util.Base64.getEncoder().encodeToString(
                                                                "wrongpassuser:wrongpassword".getBytes())))
                                .andExpect(status().isUnauthorized());
        }
}

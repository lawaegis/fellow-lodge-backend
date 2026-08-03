package com.fellowlodge.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRbacApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }

    private void expectStatus(String token, String method, String url, int expectedStatus) throws Exception {
        var request = get(url).header("Authorization", "Bearer " + token);
        mockMvc.perform(request).andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("Admin login succeeds and can access every protected module")
    void adminLoginAndFullAccess() throws Exception {
        String token = login("Admin", "Admin@123");
        assertThat(token).isNotBlank();

        expectStatus(token, "GET", "/api/users", 200);
        expectStatus(token, "GET", "/api/roles", 200);
        expectStatus(token, "GET", "/api/guests", 200);
        expectStatus(token, "GET", "/api/rooms", 200);
        expectStatus(token, "GET", "/api/reservations", 200);
        expectStatus(token, "GET", "/api/payments", 200);
        expectStatus(token, "GET", "/api/invoices", 200);
        expectStatus(token, "GET", "/api/reports/revenue", 200);
        expectStatus(token, "GET", "/api/settings", 200);
        expectStatus(token, "GET", "/api/audit-logs", 200);
        expectStatus(token, "GET", "/api/dashboard/stats", 200);
    }

    @Test
    @DisplayName("Receptionist gets operational modules but no admin modules")
    void receptionistLoginAndRestrictedAccess() throws Exception {
        String token = login("Reception", "Reception@123");
        assertThat(token).isNotBlank();

        expectStatus(token, "GET", "/api/guests", 200);
        expectStatus(token, "GET", "/api/rooms", 200);
        expectStatus(token, "GET", "/api/room-types", 200);
        expectStatus(token, "GET", "/api/reservations", 200);
        expectStatus(token, "GET", "/api/dashboard/stats", 200);

        expectStatus(token, "GET", "/api/users", 403);
        expectStatus(token, "GET", "/api/roles", 403);
        expectStatus(token, "GET", "/api/settings", 403);
        expectStatus(token, "GET", "/api/audit-logs", 403);
        expectStatus(token, "GET", "/api/reports/revenue", 403);
    }

    @Test
    @DisplayName("Accountant gets finance modules but no guest or room modules")
    void accountantLoginAndRestrictedAccess() throws Exception {
        String token = login("Accountant", "Account@123");
        assertThat(token).isNotBlank();

        expectStatus(token, "GET", "/api/payments", 200);
        expectStatus(token, "GET", "/api/invoices", 200);
        expectStatus(token, "GET", "/api/transactions", 200);
        expectStatus(token, "GET", "/api/reports/revenue", 200);
        expectStatus(token, "GET", "/api/reports/occupancy", 200);

        expectStatus(token, "GET", "/api/guests", 403);
        expectStatus(token, "GET", "/api/rooms", 403);
        expectStatus(token, "GET", "/api/reservations", 403);
        expectStatus(token, "GET", "/api/users", 403);
    }

    @Test
    @DisplayName("Unauthenticated requests are rejected with 401")
    void unauthenticatedRequestRejected() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid credentials are rejected with 401")
    void wrongPasswordRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Admin\",\"password\":\"WrongPass@999\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("Guest self-registration creates a working guest account")
    void guestRegistrationWorks() throws Exception {
        String email = "guest-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Test\",\"lastName\":\"Guest\",\"email\":\""
                                + email + "\",\"phone\":\"555-0100\",\"password\":\"GuestPass@123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + email + "\",\"password\":\"GuestPass@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value(email));
    }

    @Test
    @DisplayName("A deactivated or locked user's existing token is rejected with 401")
    void statusChangeInvalidatesExistingToken() throws Exception {
        String email = "suspend-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        MvcResult reg = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Suspend\",\"lastName\":\"Target\",\"email\":\""
                                + email + "\",\"phone\":\"555-0200\",\"password\":\"Suspend@123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode regBody = objectMapper.readTree(reg.getResponse().getContentAsString());
        String guestToken = regBody.path("data").path("accessToken").asText();
        String guestId = regBody.path("data").path("user").path("id").asText();
        assertThat(guestToken).isNotBlank();

        mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk());

        String adminToken = login("Admin", "Admin@123");

        mockMvc.perform(patch("/api/users/" + guestId + "/status?active=false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/users/" + guestId + "/status?active=true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/" + guestId + "/lock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Repeated failed logins lock the account after 5 attempts")
    void failedAttemptsLockAccount() throws Exception {
        String email = "lock-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Lock\",\"lastName\":\"Target\",\"email\":\""
                                + email + "\",\"password\":\"LockPass@123\"}"))
                .andExpect(status().isCreated());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + email + "\",\"password\":\"WrongPass@999\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + email + "\",\"password\":\"LockPass@123\"}"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locked")));
    }
}

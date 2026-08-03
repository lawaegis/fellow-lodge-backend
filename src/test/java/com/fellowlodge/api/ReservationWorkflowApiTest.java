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

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationWorkflowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String receptionToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Reception\",\"password\":\"Reception@123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }

    private String postJson(String token, String url, String json) throws Exception {
        var request = post(url).header("Authorization", "Bearer " + token);
        if (json != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request).andReturn().getResponse().getContentAsString();
    }

    private UUID createGuest(String token) throws Exception {
        String suffix = String.valueOf(System.nanoTime() % 1000000);
        String body = "{\"firstName\":\"Test\",\"lastName\":\"Guest" + suffix
                + "\",\"email\":\"test.guest" + suffix + "@example.com\",\"phone\":\"+1555000"
                + suffix.substring(0, Math.min(5, suffix.length())) + "\"}";
        String response = postJson(token, "/api/guests", body);
        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }

    private UUID createRoom(String token) throws Exception {
        String roomNumber = "WF" + (System.currentTimeMillis() % 100000);
        String body = "{\"roomNumber\":\"" + roomNumber + "\",\"floor\":5,"
                + "\"pricePerNight\":120.00,\"status\":\"Available\"}";
        String response = postJson(token, "/api/rooms", body);
        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }

    @Test
    @DisplayName("Full reservation lifecycle: create, approve, check-in, check-out")
    void fullReservationLifecycle() throws Exception {
        String token = receptionToken();
        UUID guestId = createGuest(token);
        UUID roomId = createRoom(token);
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        String createBody = "{\"guestId\":\"" + guestId + "\",\"roomId\":\"" + roomId
                + "\",\"checkInDate\":\"" + today + "\",\"checkOutDate\":\"" + tomorrow
                + "\",\"numberOfGuests\":2,\"source\":\"DESKTOP\"}";
        String createResponse = postJson(token, "/api/reservations", createBody);
        JsonNode reservation = objectMapper.readTree(createResponse).path("data");
        UUID reservationId = UUID.fromString(reservation.path("id").asText());
        assertThat(reservation.path("status").asText()).isEqualTo("Pending");
        assertThat(reservation.path("totalAmount").asDouble()).isGreaterThan(0);

        String approveResponse = postJson(token, "/api/reservations/" + reservationId + "/approve", null);
        assertThat(objectMapper.readTree(approveResponse).path("data").path("status").asText())
                .isEqualTo("Confirmed");

        mockMvc.perform(get("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Reserved"));

        String checkInBody = "{\"reservationId\":\"" + reservationId
                + "\",\"roomCondition\":\"Clean\"}";
        String checkInResponse = postJson(token, "/api/reservations/check-in", checkInBody);
        JsonNode checkIn = objectMapper.readTree(checkInResponse).path("data");
        UUID checkInId = UUID.fromString(checkIn.path("id").asText());

        mockMvc.perform(get("/api/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CheckedIn"));

        String checkOutBody = "{\"checkInId\":\"" + checkInId
                + "\",\"miniBarCharges\":15.50,\"damageCharges\":0,\"otherCharges\":0}";
        String checkOutResponse = postJson(token, "/api/reservations/check-out", checkOutBody);
        JsonNode checkOut = objectMapper.readTree(checkOutResponse).path("data");
        assertThat(checkOut.path("totalAdditionalCharges").asDouble()).isEqualTo(15.50);

        mockMvc.perform(get("/api/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CheckedOut"));

        mockMvc.perform(get("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Cleaning"));
    }
}

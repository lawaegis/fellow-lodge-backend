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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Module 7: content management. Verifies the Admin CRUD surfaces for the new
 * content modules and that every write is immediately visible on the public
 * guest portal endpoints (no caching, no mock data - live DB reads).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentManagementApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin room CRUD reflects immediately on the public portal")
    void adminRoomCrudReflectsOnPublicPortal() throws Exception {
        String roomNumber = "CT" + (System.currentTimeMillis() % 100000);
        String admin = adminToken();

        MvcResult created = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomNumber\":\"" + roomNumber + "\",\"floor\":6,"
                                + "\"pricePerNight\":250.00,\"status\":\"Available\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String roomId = data(created).get("id").asText();

        JsonNode rooms = publicList("/api/public/rooms");
        JsonNode room = findRoom(rooms, roomNumber);
        assertThat(room).as("public room list contains the newly created room").isNotNull();
        assertThat(room.get("pricePerNight").decimalValue()).isEqualByComparingTo(new BigDecimal("250.00"));

        mockMvc.perform(put("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomNumber\":\"" + roomNumber + "\",\"floor\":6,"
                                + "\"pricePerNight\":315.50,\"status\":\"Available\"}"))
                .andExpect(status().isOk());

        room = findRoom(publicList("/api/public/rooms"), roomNumber);
        assertThat(room.get("pricePerNight").decimalValue()).isEqualByComparingTo(new BigDecimal("315.50"));

        mockMvc.perform(post("/api/room-images")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"" + roomId + "\",\"url\":\"/uploads/rooms/ct-1.jpg\","
                                + "\"caption\":\"CT\",\"primary\":true,\"sortOrder\":1}"))
                .andExpect(status().isCreated());

        JsonNode detail = publicDetail("/api/public/rooms/" + roomId);
        assertThat(detail.get("images").toString()).contains("/uploads/rooms/ct-1.jpg");

        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/rooms/" + roomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin menu CRUD syncs to the public menu endpoint")
    void adminMenuCrudSyncsToPublicMenu() throws Exception {
        String suffix = String.valueOf(System.nanoTime() % 1000000);
        String categoryName = "CT-Cat-" + suffix;
        String itemName = "CT-Item-" + suffix;
        String admin = adminToken();

        MvcResult catResult = mockMvc.perform(post("/api/menu-categories")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + categoryName + "\",\"description\":\"CT category\","
                                + "\"sortOrder\":99}"))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = data(catResult).get("id").asText();

        MvcResult itemResult = mockMvc.perform(post("/api/menu-items")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + itemName + "\",\"price\":12.99,"
                                + "\"description\":\"CT dish\",\"categoryId\":\"" + categoryId + "\","
                                + "\"ingredients\":\"Salt\",\"available\":true}"))
                .andExpect(status().isCreated())
                .andReturn();
        String itemId = data(itemResult).get("id").asText();

        JsonNode menu = data(mockMvc.perform(get("/api/public/menu"))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(findInArray(menu.get("categories"), "name", categoryName))
                .as("public menu exposes the new category").isNotNull();
        assertThat(findInArray(menu.get("items"), "name", itemName))
                .as("public menu exposes the new item").isNotNull();

        mockMvc.perform(post("/api/menu-items/" + itemId + "/active?active=false")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        JsonNode menuAfter = data(mockMvc.perform(get("/api/public/menu"))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(findInArray(menuAfter.get("items"), "name", itemName))
                .as("deactivated item disappears from the public menu").isNull();
    }

    @Test
    @DisplayName("Admin banners, announcements, policies and FAQs reach the public portal")
    void adminContentReachesPublicPortal() throws Exception {
        String suffix = String.valueOf(System.nanoTime() % 1000000);
        String admin = adminToken();
        String bannerTitle = "CT-Banner-" + suffix;
        String policyTitle = "CT-Policy-" + suffix;
        String faqQuestion = "CT-FAQ-" + suffix + "?";
        String annTitle = "CT-Ann-" + suffix;

        MvcResult bannerResult = mockMvc.perform(post("/api/banners")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + bannerTitle + "\",\"subtitle\":\"CT sub\","
                                + "\"imageUrl\":\"/uploads/banners/ct.jpg\",\"linkUrl\":\"/rooms\","
                                + "\"position\":\"home\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String bannerId = data(bannerResult).get("id").asText();

        mockMvc.perform(post("/api/policies")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + policyTitle + "\",\"category\":\"Booking\","
                                + "\"content\":\"CT policy content.\",\"sortOrder\":99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/faqs")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"" + faqQuestion + "\",\"answer\":\"CT answer.\","
                                + "\"category\":\"General\",\"sortOrder\":99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + annTitle + "\",\"message\":\"CT message\","
                                + "\"priority\":\"Medium\"}"))
                .andExpect(status().isCreated());

        assertThat(findInArray(publicList("/api/public/banners"), "title", bannerTitle))
                .as("new banner appears on the portal").isNotNull();
        assertThat(findInArray(publicList("/api/public/policies"), "title", policyTitle))
                .as("new policy appears on the portal").isNotNull();
        assertThat(findInArray(publicList("/api/public/faqs"), "question", faqQuestion))
                .as("new FAQ appears on the portal").isNotNull();
        assertThat(findInArray(publicList("/api/public/announcements"), "title", annTitle))
                .as("new announcement appears on the portal").isNotNull();

        mockMvc.perform(post("/api/banners/" + bannerId + "/active?active=false")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        assertThat(findInArray(publicList("/api/public/banners"), "title", bannerTitle))
                .as("deactivated banner disappears from the portal").isNull();
    }

    @Test
    @DisplayName("Admin amenity CRUD works without a client-supplied id")
    void adminAmenityCrudWorks() throws Exception {
        String suffix = String.valueOf(System.nanoTime() % 1000000);
        String amenityName = "CT-Amenity-" + suffix;
        String admin = adminToken();

        MvcResult created = mockMvc.perform(post("/api/amenities")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + amenityName + "\",\"icon\":\"spa\","
                                + "\"description\":\"CT amenity description\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String amenityId = data(created).get("id").asText();
        assertThat(amenityId)
                .as("server generates an id for the new amenity").isNotBlank();

        assertThat(findInArray(adminList("/api/amenities"), "name", amenityName))
                .as("new amenity appears in the admin list").isNotNull();

        mockMvc.perform(put("/api/amenities/" + amenityId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + amenityName + "\",\"icon\":\"gym\","
                                + "\"description\":\"CT amenity updated\"}"))
                .andExpect(status().isOk());

        JsonNode updated = findInArray(adminList("/api/amenities"), "name", amenityName);
        assertThat(updated.get("description").asText())
                .as("updated description is persisted").isEqualTo("CT amenity updated");

        mockMvc.perform(delete("/api/amenities/" + amenityId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        assertThat(findInArray(adminList("/api/amenities"), "name", amenityName))
                .as("deleted amenity disappears from the admin list").isNull();
    }

    @Test
    @DisplayName("Role-based permissions gate the content write endpoints")
    void roleBasedAccessControlGatesContentWrites() throws Exception {
        String reception = receptionToken();
        String suffix = String.valueOf(System.nanoTime() % 1000000);

        mockMvc.perform(post("/api/menu-items")
                        .header("Authorization", "Bearer " + reception)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"CT-R-Item-" + suffix + "\",\"price\":5.00,\"available\":true}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/banners")
                        .header("Authorization", "Bearer " + reception)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"CT-Forbidden-" + suffix + "\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/policies")
                        .header("Authorization", "Bearer " + reception)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"CT-Forbidden-" + suffix + "\",\"content\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    private String adminToken() throws Exception {
        return login("Admin", "Admin@123");
    }

    private String receptionToken() throws Exception {
        return login("Reception", "Reception@123");
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return data(result).get("accessToken").asText();
    }

    private JsonNode publicList(String url) throws Exception {
        return data(mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode publicDetail(String url) throws Exception {
        return data(mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode adminList(String url) throws Exception {
        return data(mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    private JsonNode findRoom(JsonNode rooms, String roomNumber) {
        for (JsonNode node : rooms) {
            if (roomNumber.equals(node.get("roomNumber").asText())) {
                return node;
            }
        }
        return null;
    }

    private JsonNode findInArray(JsonNode array, String field, String value) {
        if (array == null) {
            return null;
        }
        for (JsonNode node : array) {
            if (value.equals(node.get(field).asText())) {
                return node;
            }
        }
        return null;
    }
}

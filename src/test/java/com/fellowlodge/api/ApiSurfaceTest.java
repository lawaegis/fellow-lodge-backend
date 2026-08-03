package com.fellowlodge.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Module 3 audit: asserts the full REST surface required by the migration plan
 * is registered and reachable. Route registration is verified against the
 * RequestMappingHandlerMapping (data-independent); live smoke checks confirm
 * the primary collection endpoints respond as Admin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSurfaceTest {

    private static final String[] REQUIRED_BASES = {
            "/api/auth", "/api/users", "/api/roles", "/api/guests", "/api/rooms",
            "/api/room-types", "/api/reservations", "/api/booking", "/api/check-ins",
            "/api/check-outs", "/api/payments", "/api/invoices", "/api/staff",
            "/api/events", "/api/event-bookings", "/api/conference-halls",
            "/api/reports", "/api/settings", "/api/audit-logs", "/api/dashboard",
            "/api/notifications", "/api/maintenance", "/api/housekeeping",
            "/api/hotel-services", "/api/transactions", "/api/reviews",
            "/api/gallery", "/api/promotions", "/api/amenities",
            "/api/menu-categories", "/api/menu-items", "/api/event-packages",
            "/api/conference-packages", "/api/banners", "/api/announcements",
            "/api/policies", "/api/faqs", "/api/room-images", "/api/attractions",
            "/api/legal-docs", "/api/service-bookings", "/api/files", "/api/public"
    };

    private static final String[] LIVE_SMOKE_ENDPOINTS = {
            "/api/users", "/api/roles", "/api/guests", "/api/rooms", "/api/room-types",
            "/api/reservations", "/api/check-ins", "/api/payments", "/api/invoices",
            "/api/staff", "/api/events", "/api/event-bookings", "/api/conference-halls",
            "/api/settings", "/api/audit-logs", "/api/booking/cart", "/api/reports/revenue",
            "/api/reports/occupancy", "/api/menu-categories", "/api/menu-items",
            "/api/event-packages", "/api/conference-packages", "/api/banners",
            "/api/announcements", "/api/policies", "/api/faqs", "/api/room-images",
            "/api/attractions", "/api/legal-docs", "/api/service-bookings",
            "/api/notifications", "/api/gallery", "/api/reviews", "/api/amenities",
            "/api/hotel-services", "/api/files"
    };

    private static final String[] PUBLIC_CONTRACT_ENDPOINTS = {
            "/api/public/room-types", "/api/public/amenities", "/api/public/rooms",
            "/api/public/services", "/api/public/events", "/api/public/conference-halls",
            "/api/public/promotions", "/api/public/gallery", "/api/public/reviews",
            "/api/public/menu", "/api/public/event-packages", "/api/public/conference-packages",
            "/api/public/banners", "/api/public/announcements", "/api/public/policies",
            "/api/public/faqs", "/api/public/hotel", "/api/public/hotel/about",
            "/api/public/hotel/contact", "/api/public/hotel/faqs",
            "/api/public/hotel/hero-banners", "/api/public/hotel/background",
            "/api/public/hotel/attractions"
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    @DisplayName("Every module required by the migration plan has registered REST routes")
    void everyRequiredModuleIsRegistered() {
        Map<RequestMappingInfo, ?> mappings = handlerMapping.getHandlerMethods();
        var patterns = mappings.keySet().stream()
                .flatMap(info -> {
                    if (info.getPathPatternsCondition() != null) {
                        return info.getPathPatternsCondition().getPatterns().stream()
                                .map(p -> p.getPatternString());
                    }
                    if (info.getPatternsCondition() != null) {
                        return info.getPatternsCondition().getPatterns().stream()
                                .map(Object::toString);
                    }
                    return java.util.stream.Stream.empty();
                })
                .toList();

        for (String base : REQUIRED_BASES) {
            assertThat(patterns)
                    .as("required module base path %s", base)
                    .anyMatch(pattern -> pattern.equals(base) || pattern.startsWith(base + "/"));
        }
    }

    @Test
    @DisplayName("Authentication endpoint accepts login requests")
    void authenticationEndpointWorks() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Admin\",\"password\":\"Admin@123\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("accessToken");
    }

    @Test
    @DisplayName("Primary collection endpoints respond as Admin")
    void primaryCollectionEndpointsRespond() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Admin\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = login.getResponse().getContentAsString();
        String token = body.split("\"accessToken\":\"")[1].split("\"")[0];

        for (String endpoint : LIVE_SMOKE_ENDPOINTS) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(endpoint)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Test
    @DisplayName("Guest portal public contract serves live data without authentication")
    void publicContractServesLiveData() throws Exception {
        for (String endpoint : PUBLIC_CONTRACT_ENDPOINTS) {
            MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get(endpoint))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            assertThat(result.getResponse().getContentAsString())
                    .as("public endpoint %s must return a JSON body", endpoint)
                    .isNotBlank();
        }
    }
}

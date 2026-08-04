package com.fellowlodge.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Version metadata contract. The desktop client polls this endpoint to decide
 * whether an update is required, so it must stay public and stable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VersionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Version endpoint serves metadata without authentication")
    void versionEndpointIsPublicAndComplete() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = body.get("data");
        assertThat(body.get("success").asBoolean()).isTrue();
        assertThat(data.get("application").asText()).isEqualTo("fellow-lodge");
        assertThat(data.get("component").asText()).isEqualTo("backend");
        assertThat(data.get("version").asText()).isNotBlank();
        assertThat(data.get("minimumDesktopVersion").asText()).isNotBlank();
        assertThat(data.get("updateIntervalDays").asLong()).isEqualTo(90);
    }
}

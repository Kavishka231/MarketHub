package com.markethub.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeDiagnosticsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generatesRequestIdAndReportsDatabaseReadiness() throws Exception {
        mockMvc.perform(get("/api/readiness"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", matchesPattern("[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void acceptsOnlyBoundedSafeIncomingRequestIds() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Request-ID", "deploy-check-42"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", "deploy-check-42"));

        mockMvc.perform(get("/actuator/health").header("X-Request-ID", "unsafe id with spaces"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", matchesPattern("[0-9a-f-]{36}")));
    }

    @Test
    void sensitiveActuatorEndpointsAreNotPubliclyExposed() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }
}
package com.markethub.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "GIT_SHA=abc1234",
        "BUILD_TIME=2026-09-19T12:00:00Z",
        "APP_ENVIRONMENT=test",
        "management.endpoints.web.exposure.include=health,info",
        "management.endpoint.info.access=unrestricted",
        "management.info.env.enabled=true",
        "info.app.name=markethub-api",
        "info.app.version=0.0.1-SNAPSHOT",
        "info.app.gitSha=${GIT_SHA}",
        "info.app.buildTime=${BUILD_TIME}",
        "info.app.environment=${APP_ENVIRONMENT}"
})
@AutoConfigureMockMvc
class BuildInfoTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesSafePublicBuildMetadata() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("markethub-api"))
                .andExpect(jsonPath("$.app.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.app.gitSha").value("abc1234"))
                .andExpect(jsonPath("$.app.buildTime").value("2026-09-19T12:00:00Z"))
                .andExpect(jsonPath("$.app.environment").value("test"));
    }

    @Test
    void buildInfoDoesNotExposeRuntimeSecrets() throws Exception {
        String response = mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.assertj.core.api.Assertions.assertThat(response)
                .doesNotContain("password", "secret", "jdbc:", "Authorization");
    }
}

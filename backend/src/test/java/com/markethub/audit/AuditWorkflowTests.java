package com.markethub.audit;

import com.markethub.auth.JwtService;
import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditWorkflowTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuditLogRepository logs;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;

    @Test
    void recordsAdminMutationWithActorResourceAndRequestId() throws Exception {
        User admin = user(UserRole.ADMIN);
        User customer = user(UserRole.CUSTOMER);
        mvc.perform(patch("/api/admin/users/{id}/disable", customer.getId())
                        .header("Authorization", token(admin))
                        .header("X-Request-ID", "audit-request-42"))
                .andExpect(status().isOk());

        AuditLog log = logs.findAll().stream()
                .filter(item -> "audit-request-42".equals(item.getRequestId()))
                .findFirst().orElseThrow();
        assertThat(log.getActorUserId()).isEqualTo(admin.getId());
        assertThat(log.getActorRole()).isEqualTo("ADMIN");
        assertThat(log.getAction()).isEqualTo("ADMIN_USER_DISABLE");
        assertThat(log.getResourceType()).isEqualTo("USER");
        assertThat(log.getResourceId()).isEqualTo(customer.getId().toString());
        assertThat(log.getMetadata()).doesNotContain("password", "token", "Authorization");
    }

    @Test
    void adminCanFilterAuditLogsWhileCustomerIsForbidden() throws Exception {
        User admin = user(UserRole.ADMIN);
        User customer = user(UserRole.CUSTOMER);
        mvc.perform(get("/api/admin/audit-logs").header("Authorization", token(admin))
                        .param("action", "ADMIN_USER_DISABLE").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
        mvc.perform(get("/api/admin/audit-logs").header("Authorization", token(customer)))
                .andExpect(status().isForbidden());
    }

    private User user(UserRole role) {
        String key = UUID.randomUUID().toString();
        return users.saveAndFlush(new User("Audit", "User", key + "@example.com", encoder.encode("Password123"), role));
    }

    private String token(User user) { return "Bearer " + jwt.generateToken(user); }
}
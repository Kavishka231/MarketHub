package com.markethub.vendor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markethub.auth.JwtService;
import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminVendorWorkflowTests {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void customerCannotAccessAdminVendorEndpoints() throws Exception {
        assertAdminEndpointsForbidden(createUser("customer@example.com", UserRole.CUSTOMER));
    }

    @Test
    void vendorCannotAccessAdminVendorEndpoints() throws Exception {
        assertAdminEndpointsForbidden(createUser("vendor@example.com", UserRole.VENDOR));
    }

    @Test
    void adminCanListAndFilterPendingVendorApplications() throws Exception {
        User admin = createUser("admin-list@example.com", UserRole.ADMIN);
        Vendor pending = createVendor(createUser("pending@example.com", UserRole.CUSTOMER), "Pending Store");
        Vendor rejected = createVendor(createUser("rejected@example.com", UserRole.CUSTOMER), "Rejected Store");
        rejected.setStatus(VendorStatus.REJECTED);
        vendorRepository.saveAndFlush(rejected);

        mockMvc.perform(get("/api/admin/vendors").header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/admin/vendors")
                        .queryParam("status", "PENDING")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(pending.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void invalidStatusFilterReturnsBadRequest() throws Exception {
        User admin = createUser("admin-invalid-filter@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/vendors")
                        .queryParam("status", "UNKNOWN")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.status").value("Invalid value: UNKNOWN"));
    }

    @Test
    void approvalPersistsApprovedStatusAndVendorRole() throws Exception {
        User admin = createUser("admin-approve@example.com", UserRole.ADMIN);
        User customer = createUser("approval@example.com", UserRole.CUSTOMER);
        Vendor vendor = createVendor(customer, "Approval Store");

        mockMvc.perform(patch("/api/admin/vendors/{vendorId}/approve", vendor.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        assertThat(vendorRepository.findById(vendor.getId()).orElseThrow().getStatus())
                .isEqualTo(VendorStatus.APPROVED);
        assertThat(userRepository.findById(customer.getId()).orElseThrow().getRole())
                .isEqualTo(UserRole.VENDOR);
    }

    @Test
    void rejectionPersistsRejectedStatusAndKeepsCustomerRole() throws Exception {
        User admin = createUser("admin-reject@example.com", UserRole.ADMIN);
        User customer = createUser("rejection@example.com", UserRole.CUSTOMER);
        Vendor vendor = createVendor(customer, "Rejected Store");

        mockMvc.perform(patch("/api/admin/vendors/{vendorId}/reject", vendor.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertThat(vendorRepository.findById(vendor.getId()).orElseThrow().getStatus())
                .isEqualTo(VendorStatus.REJECTED);
        assertThat(userRepository.findById(customer.getId()).orElseThrow().getRole())
                .isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void approvedVendorCannotBeApprovedAgainOrRejected() throws Exception {
        User admin = createUser("admin-approved-transition@example.com", UserRole.ADMIN);
        Vendor vendor = createVendor(createUser("approved-transition@example.com", UserRole.CUSTOMER), "Store");
        process(admin, vendor, "approve").andExpect(status().isOk());

        process(admin, vendor, "approve").andExpect(status().isConflict());
        process(admin, vendor, "reject").andExpect(status().isConflict());
    }

    @Test
    void rejectedVendorCannotBeRejectedAgainOrApproved() throws Exception {
        User admin = createUser("admin-rejected-transition@example.com", UserRole.ADMIN);
        Vendor vendor = createVendor(createUser("rejected-transition@example.com", UserRole.CUSTOMER), "Store");
        process(admin, vendor, "reject").andExpect(status().isOk());

        process(admin, vendor, "reject").andExpect(status().isConflict());
        process(admin, vendor, "approve").andExpect(status().isConflict());
    }

    @Test
    void missingVendorReturnsNotFound() throws Exception {
        User admin = createUser("admin-missing@example.com", UserRole.ADMIN);

        mockMvc.perform(patch("/api/admin/vendors/{vendorId}/approve", Long.MAX_VALUE)
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void approvalRequiresLoginAgainForJwtRoleClaimToChange() throws Exception {
        User admin = createUser("admin-token@example.com", UserRole.ADMIN);
        User customer = createUser("token-customer@example.com", UserRole.CUSTOMER);
        Vendor vendor = createVendor(customer, "Token Store");
        String tokenIssuedBeforeApproval = jwtService.generateToken(customer);

        process(admin, vendor, "approve").andExpect(status().isOk());

        assertThat(roleClaim(tokenIssuedBeforeApproval)).isEqualTo("CUSTOMER");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "token-customer@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("VENDOR"))
                .andReturn();
        String refreshedToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(roleClaim(refreshedToken)).isEqualTo("VENDOR");
    }

    private void assertAdminEndpointsForbidden(User user) throws Exception {
        mockMvc.perform(get("/api/admin/vendors").header("Authorization", bearerToken(user)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/vendors/1/approve").header("Authorization", bearerToken(user)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/vendors/1/reject").header("Authorization", bearerToken(user)))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions process(
            User admin, Vendor vendor, String action) throws Exception {
        return mockMvc.perform(patch("/api/admin/vendors/{vendorId}/{action}", vendor.getId(), action)
                .header("Authorization", bearerToken(admin)));
    }

    private User createUser(String email, UserRole role) {
        User user = new User("Test", "User", email, passwordEncoder.encode(PASSWORD), role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    private Vendor createVendor(User user, String storeName) {
        return vendorRepository.saveAndFlush(new Vendor(user, storeName, "Description", "0771234567"));
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private String roleClaim(String token) throws Exception {
        String payload = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[1]),
                StandardCharsets.UTF_8);
        JsonNode claims = objectMapper.readTree(payload);
        return claims.get("role").asText();
    }
}

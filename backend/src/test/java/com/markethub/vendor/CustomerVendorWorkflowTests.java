package com.markethub.vendor;

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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerVendorWorkflowTests {

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

    @Test
    void customerApplicationUsesAuthenticatedUserAndStartsPending() throws Exception {
        User customer = createUser("applicant@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);

        mockMvc.perform(post("/api/vendors/apply")
                        .header("Authorization", bearerToken(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeName": "  Island Market  ",
                                  "description": "  Local products  ",
                                  "phone": "  +94 77 123 4567  ",
                                  "status": "APPROVED",
                                  "userId": 999,
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(customer.getId()))
                .andExpect(jsonPath("$.storeName").value("Island Market"))
                .andExpect(jsonPath("$.description").value("Local products"))
                .andExpect(jsonPath("$.phone").value("+94 77 123 4567"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Vendor savedVendor = vendorRepository.findByUserId(customer.getId()).orElseThrow();
        assertThat(savedVendor.getUser().getId()).isEqualTo(customer.getId());
        assertThat(savedVendor.getStatus()).isEqualTo(VendorStatus.PENDING);
        assertThat(userRepository.findById(customer.getId()).orElseThrow().getRole())
                .isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void blankStoreNameReturnsBadRequest() throws Exception {
        User customer = createUser("blank-store@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);

        apply(customer, "   ", "0771234567")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.storeName").value("Store name is required"));
    }

    @Test
    void invalidPhoneReturnsBadRequest() throws Exception {
        User customer = createUser("invalid-phone@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);

        apply(customer, "Phone Store", "not-a-phone")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.phone").value("Phone number must be valid"));
    }

    @Test
    void duplicateApplicationReturnsConflict() throws Exception {
        User customer = createUser("duplicate-vendor@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);

        apply(customer, "First Store", "0771234567").andExpect(status().isCreated());
        apply(customer, "Second Store", "0771234567")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Vendor application already exists"));
    }

    @Test
    void disabledCustomerCannotApply() throws Exception {
        User customer = createUser("inactive@example.com", UserRole.CUSTOMER, UserStatus.DISABLED);

        apply(customer, "Inactive Store", "0771234567")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonCustomerCannotApply() throws Exception {
        User admin = createUser("admin-apply@example.com", UserRole.ADMIN, UserStatus.ACTIVE);

        apply(admin, "Admin Store", "0771234567")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only customers can apply to become vendors"));
    }

    @Test
    void unauthenticatedUserCannotApply() throws Exception {
        mockMvc.perform(post("/api/vendors/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationRequest("Private Store", "0771234567")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentVendorReturnsOnlyAuthenticatedUsersApplication() throws Exception {
        User firstCustomer = createUser("first-vendor@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);
        User secondCustomer = createUser("second-vendor@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);
        Vendor firstVendor = createVendor(firstCustomer, "First Store");
        createVendor(secondCustomer, "Second Store");

        mockMvc.perform(get("/api/vendors/me")
                        .header("Authorization", bearerToken(firstCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstVendor.getId()))
                .andExpect(jsonPath("$.userId").value(firstCustomer.getId()))
                .andExpect(jsonPath("$.storeName").value("First Store"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void currentVendorReturnsNotFoundWhenApplicationIsMissing() throws Exception {
        User customer = createUser("missing-vendor@example.com", UserRole.CUSTOMER, UserStatus.ACTIVE);

        mockMvc.perform(get("/api/vendors/me")
                        .header("Authorization", bearerToken(customer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vendor application not found"));
    }

    private org.springframework.test.web.servlet.ResultActions apply(
            User user, String storeName, String phone) throws Exception {
        return mockMvc.perform(post("/api/vendors/apply")
                .header("Authorization", bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(applicationRequest(storeName, phone)));
    }

    private User createUser(String email, UserRole role, UserStatus status) {
        User user = new User("Test", "User", email, passwordEncoder.encode(PASSWORD), role);
        user.setStatus(status);
        return userRepository.saveAndFlush(user);
    }

    private Vendor createVendor(User user, String storeName) {
        return vendorRepository.saveAndFlush(new Vendor(user, storeName, "Description", "0771234567"));
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private String applicationRequest(String storeName, String phone) {
        return """
                {
                  "storeName": "%s",
                  "description": "Store description",
                  "phone": "%s"
                }
                """.formatted(storeName, phone);
    }
}

package com.markethub.address;

import com.markethub.auth.JwtService;
import com.markethub.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AddressWorkflowTests {
    @Autowired MockMvc mvc;
    @Autowired AddressRepository addresses;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;

    @Test void addressPersistsWithCorrectCustomer() {
        User customer = user("address-persist@example.com", UserRole.CUSTOMER);
        Address saved = addresses.saveAndFlush(address(customer, "Persisted", true));
        Address found = addresses.findById(saved.getId()).orElseThrow();
        assertThat(found.getFullName()).isEqualTo("Persisted");
        assertThat(found.getUser().getId()).isEqualTo(customer.getId());
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test void customerCreatesNormalizedAddressAndFirstBecomesDefault() throws Exception {
        User customer = user("address-create@example.com", UserRole.CUSTOMER);
        mvc.perform(post("/api/addresses").header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content(body("  Jane   Doe  ", false)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.defaultAddress").value(true));
        Address saved = addresses.findByUserIdOrderByCreatedAtAsc(customer.getId()).get(0);
        assertThat(saved.getUser().getId()).isEqualTo(customer.getId());
    }

    @Test void unauthenticatedAndVendorUsersAreRejected() throws Exception {
        mvc.perform(post("/api/addresses").contentType(MediaType.APPLICATION_JSON).content(body("No Auth", false)))
                .andExpect(status().isUnauthorized());
        User vendor = user("address-vendor@example.com", UserRole.VENDOR);
        mvc.perform(get("/api/addresses").header("Authorization", token(vendor)))
                .andExpect(status().isForbidden());
    }

    @Test void requiredFieldsAreValidated() throws Exception {
        User customer = user("address-validation@example.com", UserRole.CUSTOMER);
        mvc.perform(post("/api/addresses").header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"fullName":" ","phone":"","addressLine1":"","city":"","district":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test void secondRequestedDefaultClearsFirstAndListingPlacesDefaultFirst() throws Exception {
        User customer = user("address-defaults@example.com", UserRole.CUSTOMER);
        create(customer, "First", false).andExpect(status().isCreated());
        create(customer, "Second", true).andExpect(status().isCreated());
        assertThat(addresses.findByUserIdOrderByCreatedAtAsc(customer.getId()))
                .hasSize(2).filteredOn(Address::isDefaultAddress).hasSize(1)
                .first().extracting(Address::getFullName).isEqualTo("Second");
        mvc.perform(get("/api/addresses").header("Authorization", token(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].fullName").value("Second"))
                .andExpect(jsonPath("$[0].defaultAddress").value(true));
    }

    @Test void listingReturnsOnlyAuthenticatedCustomersAddresses() throws Exception {
        User first = user("address-list-first@example.com", UserRole.CUSTOMER);
        User second = user("address-list-second@example.com", UserRole.CUSTOMER);
        addresses.saveAndFlush(address(first, "First customer", true));
        addresses.saveAndFlush(address(second, "Second customer", true));
        mvc.perform(get("/api/addresses").header("Authorization", token(first)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("First customer"));
    }

    @Test void customerUpdatesOwnAddressButCannotUpdateForeignOrMissingAddress() throws Exception {
        User owner = user("address-update-owner@example.com", UserRole.CUSTOMER);
        User other = user("address-update-other@example.com", UserRole.CUSTOMER);
        Address own = addresses.saveAndFlush(address(owner, "Old Name", true));
        Address foreign = addresses.saveAndFlush(address(other, "Private", true));
        mvc.perform(put("/api/addresses/{id}", own.getId()).header("Authorization", token(owner))
                .contentType(MediaType.APPLICATION_JSON).content(updateBody("Updated Name")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Updated Name"));
        mvc.perform(put("/api/addresses/{id}", foreign.getId()).header("Authorization", token(owner))
                .contentType(MediaType.APPLICATION_JSON).content(updateBody("No")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/addresses/{id}", Long.MAX_VALUE).header("Authorization", token(owner))
                .contentType(MediaType.APPLICATION_JSON).content(updateBody("Missing")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Address not found"));
    }

    @Test void customerDeletesOwnAddressButCannotDeleteAnotherCustomersAddress() throws Exception {
        User owner = user("address-delete-owner@example.com", UserRole.CUSTOMER);
        User other = user("address-delete-other@example.com", UserRole.CUSTOMER);
        Address own = addresses.saveAndFlush(address(owner, "Delete me", true));
        Address foreign = addresses.saveAndFlush(address(other, "Keep me", true));
        mvc.perform(delete("/api/addresses/{id}", foreign.getId()).header("Authorization", token(owner)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/addresses/{id}", own.getId()).header("Authorization", token(owner)))
                .andExpect(status().isNoContent());
        assertThat(addresses.findById(own.getId())).isEmpty();
        assertThat(addresses.findById(foreign.getId())).isPresent();
    }

    @Test void deletingDefaultPromotesOldestRemainingAndDeletingFinalIsSafe() throws Exception {
        User customer = user("address-delete-default@example.com", UserRole.CUSTOMER);
        Address first = addresses.saveAndFlush(address(customer, "Oldest", false));
        Address second = addresses.saveAndFlush(address(customer, "Second", false));
        Address currentDefault = addresses.saveAndFlush(address(customer, "Current", true));
        mvc.perform(delete("/api/addresses/{id}", currentDefault.getId()).header("Authorization", token(customer)))
                .andExpect(status().isNoContent());
        assertThat(addresses.findById(first.getId()).orElseThrow().isDefaultAddress()).isTrue();
        assertThat(addresses.findById(second.getId()).orElseThrow().isDefaultAddress()).isFalse();
        mvc.perform(delete("/api/addresses/{id}", second.getId()).header("Authorization", token(customer)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/addresses/{id}", first.getId()).header("Authorization", token(customer)))
                .andExpect(status().isNoContent());
        assertThat(addresses.countByUserId(customer.getId())).isZero();
    }

    @Test void customerExplicitlyChangesDefaultAndOwnershipIsEnforced() throws Exception {
        User owner = user("address-select-owner@example.com", UserRole.CUSTOMER);
        User other = user("address-select-other@example.com", UserRole.CUSTOMER);
        Address first = addresses.saveAndFlush(address(owner, "First", true));
        Address second = addresses.saveAndFlush(address(owner, "Second", false));
        Address foreign = addresses.saveAndFlush(address(other, "Foreign", true));
        mvc.perform(patch("/api/addresses/{id}/default", second.getId()).header("Authorization", token(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.defaultAddress").value(true));
        assertThat(addresses.findById(first.getId()).orElseThrow().isDefaultAddress()).isFalse();
        assertThat(addresses.findById(second.getId()).orElseThrow().isDefaultAddress()).isTrue();
        mvc.perform(patch("/api/addresses/{id}/default", foreign.getId()).header("Authorization", token(owner)))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions create(User customer, String name, boolean defaultAddress) throws Exception {
        return mvc.perform(post("/api/addresses").header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content(body(name, defaultAddress)));
    }
    private Address address(User user, String name, boolean defaultAddress) {
        return new Address(user, name, "0771234567", "10 Main Street", null,
                "Colombo", "Colombo", "00100", defaultAddress);
    }
    private String body(String name, boolean defaultAddress) {
        return """
                {"fullName":"%s","phone":"0771234567","addressLine1":"10 Main Street","addressLine2":" Apartment 2 ","city":"Colombo","district":"Colombo","postalCode":"00100","defaultAddress":%s}
                """.formatted(name, defaultAddress);
    }
    private String updateBody(String name) {
        return """
                {"fullName":"%s","phone":"0711111111","addressLine1":"20 New Road","addressLine2":null,"city":"Kandy","district":"Kandy","postalCode":"20000"}
                """.formatted(name);
    }
    private User user(String email, UserRole role) {
        return users.saveAndFlush(new User("Address", "Tester", email, encoder.encode("Password123"), role));
    }
    private String token(User user) { return "Bearer " + jwt.generateToken(user); }
}

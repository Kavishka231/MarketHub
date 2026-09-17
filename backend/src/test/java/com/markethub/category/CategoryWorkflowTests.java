package com.markethub.category;

import com.markethub.auth.JwtService;
import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryWorkflowTests {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void unauthenticatedPublicListingReturnsOnlyActiveCategoriesAlphabetically() throws Exception {
        createCategory("Toys", "toys", true);
        createCategory("Books", "books", true);
        createCategory("Archived", "archived", false);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Books"))
                .andExpect(jsonPath("$[1].name").value("Toys"))
                .andExpect(jsonPath("$[?(@.name == 'Archived')]").isEmpty());
    }

    @Test
    void publicListingReturnsEmptyListWhenNoCategoriesExist() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void adminCanCreateCategoryWithNormalizedNameAndGeneratedSlug() throws Exception {
        User admin = createUser("category-admin@example.com", UserRole.ADMIN);

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Home   Electronics  ",
                                  "description": "  Devices and accessories  ",
                                  "id": 999,
                                  "slug": "client-slug",
                                  "active": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Home Electronics"))
                .andExpect(jsonPath("$.slug").value("home-electronics"))
                .andExpect(jsonPath("$.description").value("Devices and accessories"));

        Category saved = categoryRepository.findAll().get(0);
        assertThat(saved.getSlug()).isEqualTo("home-electronics");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void duplicateNameIsRejectedIgnoringCase() throws Exception {
        User admin = createUser("duplicate-name-admin@example.com", UserRole.ADMIN);
        createCategory("Books", "books", true);

        createRequest(admin, "books", "Other")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void duplicateGeneratedSlugIsRejected() throws Exception {
        User admin = createUser("duplicate-slug-admin@example.com", UserRole.ADMIN);
        createCategory("Home Electronics", "home-electronics", true);

        createRequest(admin, "Home---Electronics", "Other")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void blankCategoryNameIsRejected() throws Exception {
        User admin = createUser("invalid-category-admin@example.com", UserRole.ADMIN);

        createRequest(admin, "   ", "Invalid")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Category name is required"));
    }

    @Test
    void customerCannotCreateCategory() throws Exception {
        User customer = createUser("category-customer@example.com", UserRole.CUSTOMER);

        createRequest(customer, "Books", "Reading")
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest("Books", "Reading")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanUpdateNameSlugAndDescription() throws Exception {
        User admin = createUser("update-category-admin@example.com", UserRole.ADMIN);
        Category category = createCategory("Phones", "phones", true);

        mockMvc.perform(put("/api/admin/categories/{categoryId}", category.getId())
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest("Mobile Devices", "Updated description")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mobile Devices"))
                .andExpect(jsonPath("$.slug").value("mobile-devices"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getSlug()).isEqualTo("mobile-devices");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateToDuplicateNameIsRejected() throws Exception {
        User admin = createUser("duplicate-update-admin@example.com", UserRole.ADMIN);
        createCategory("Books", "books", true);
        Category toys = createCategory("Toys", "toys", true);

        updateRequest(admin, toys, "BOOKS", "Duplicate")
                .andExpect(status().isConflict());
    }

    @Test
    void updatingMissingCategoryReturnsNotFound() throws Exception {
        User admin = createUser("missing-update-admin@example.com", UserRole.ADMIN);

        mockMvc.perform(put("/api/admin/categories/{categoryId}", Long.MAX_VALUE)
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest("Missing", "None")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void adminCanDeactivateCategoryWithoutDeletingItAndHideItFromPublicListing() throws Exception {
        User admin = createUser("deactivate-admin@example.com", UserRole.ADMIN);
        Category category = createCategory("Seasonal", "seasonal", true);

        mockMvc.perform(patch("/api/admin/categories/{categoryId}/deactivate", category.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk());

        assertThat(categoryRepository.findById(category.getId())).isPresent();
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isFalse();
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void customerCannotDeactivateCategory() throws Exception {
        User customer = createUser("deactivate-customer@example.com", UserRole.CUSTOMER);
        Category category = createCategory("Protected", "protected", true);

        mockMvc.perform(patch("/api/admin/categories/{categoryId}/deactivate", category.getId())
                        .header("Authorization", bearerToken(customer)))
                .andExpect(status().isForbidden());

        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void adminCanReactivateCategoryAndRestoreItToPublicListing() throws Exception {
        User admin = createUser("activate-admin@example.com", UserRole.ADMIN);
        Category category = createCategory("Restored", "restored", false);

        mockMvc.perform(patch("/api/admin/categories/{categoryId}/activate", category.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk());

        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isTrue();
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("restored"));
    }

    private org.springframework.test.web.servlet.ResultActions createRequest(
            User user, String name, String description) throws Exception {
        return mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryRequest(name, description)));
    }

    private org.springframework.test.web.servlet.ResultActions updateRequest(
            User admin, Category category, String name, String description) throws Exception {
        return mockMvc.perform(put("/api/admin/categories/{categoryId}", category.getId())
                .header("Authorization", bearerToken(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryRequest(name, description)));
    }

    private Category createCategory(String name, String slug, boolean active) {
        Category category = new Category(name, slug, "Description");
        category.setActive(active);
        return categoryRepository.saveAndFlush(category);
    }

    private User createUser(String email, UserRole role) {
        return userRepository.saveAndFlush(
                new User("Test", "User", email, passwordEncoder.encode(PASSWORD), role));
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private String categoryRequest(String name, String description) {
        return """
                {
                  "name": "%s",
                  "description": "%s"
                }
                """.formatted(name, description);
    }
}

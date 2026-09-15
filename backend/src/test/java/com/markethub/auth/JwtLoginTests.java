package com.markethub.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtLoginTests {

    private static final String TEST_SECRET =
            "bWFya2V0aHViLWxvY2FsLWRldi1qd3Qtc2VjcmV0LWNoYW5nZS1tZQ==";
    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void logsInActiveUserAndReturnsJwtWithSafeIdentity() throws Exception {
        User user = createUser("login@example.com", UserStatus.ACTIVE);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("  LOGIN@Example.com  ", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn();

        String token = readToken(result);
        assertThat(jwtService.extractEmail(token)).isEqualTo("login@example.com");
        assertThat(jwtService.validateToken(token, new MarketHubUserPrincipal(user))).isTrue();

        Claims claims = parseClaims(token);
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(user.getId());
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejectsWrongPasswordAndUnknownEmailWithSameMessage() throws Exception {
        createUser("known@example.com", UserStatus.ACTIVE);

        assertInvalidCredentials(loginRequest("known@example.com", "WrongPassword"));
        assertInvalidCredentials(loginRequest("unknown@example.com", PASSWORD));
    }

    @Test
    void rejectsDisabledUser() throws Exception {
        createUser("disabled@example.com", UserStatus.DISABLED);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("disabled@example.com", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Account is disabled"));
    }

    @Test
    void validatesLoginRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("not-an-email", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void protectsCurrentUserAndAcceptsValidToken() throws Exception {
        User user = createUser("current@example.com", UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("current@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsInvalidAndExpiredTokens() throws Exception {
        User user = createUser("token@example.com", UserStatus.ACTIVE);
        String validToken = jwtService.generateToken(user);
        String invalidToken = validToken.substring(0, validToken.length() - 1) + "x";
        String expiredToken = expiredToken(user);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());

        assertThat(jwtService.validateToken(invalidToken, new MarketHubUserPrincipal(user))).isFalse();
        assertThat(jwtService.validateToken(expiredToken, new MarketHubUserPrincipal(user))).isFalse();
    }

    private User createUser(String email, UserStatus status) {
        User user = new User(
                "Test",
                "Customer",
                email,
                passwordEncoder.encode(PASSWORD),
                UserRole.CUSTOMER);
        user.setStatus(status);
        return userRepository.saveAndFlush(user);
    }

    private void assertInvalidCredentials(String request) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    private String readToken(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(testSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String expiredToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(testSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey testSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    }

    private String loginRequest(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}

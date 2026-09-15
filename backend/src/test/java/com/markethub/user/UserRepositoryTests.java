package com.markethub.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTests {

    private static final String STORED_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsAndFindsUserByEmail() {
        User user = new User("Maya", "Perera", "maya@example.test", STORED_PASSWORD_HASH, UserRole.CUSTOMER);
        user.setPhone("+94 77 000 0000");

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(userRepository.findByEmail("maya@example.test"))
                .isPresent()
                .get()
                .extracting(User::getFirstName, User::getLastName, User::getPhone)
                .containsExactly("Maya", "Perera", "+94 77 000 0000");
        assertThat(userRepository.existsByEmail("maya@example.test")).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(
                new User("Nimal", "Silva", "shared@example.test", STORED_PASSWORD_HASH, UserRole.CUSTOMER));

        User duplicate = new User("Kamal", "Fernando", "shared@example.test", STORED_PASSWORD_HASH, UserRole.VENDOR);

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistsRoleAndStatusAsStrings() {
        User user = new User("Asha", "Fernando", "asha@example.test", STORED_PASSWORD_HASH, UserRole.ADMIN);
        user.setStatus(UserStatus.DISABLED);
        User savedUser = userRepository.saveAndFlush(user);

        String role = jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE id = ?", String.class, savedUser.getId());
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM users WHERE id = ?", String.class, savedUser.getId());

        assertThat(role).isEqualTo("ADMIN");
        assertThat(status).isEqualTo("DISABLED");
    }
}

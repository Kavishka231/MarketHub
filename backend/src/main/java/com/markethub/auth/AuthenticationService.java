package com.markethub.auth;

import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new AccountDisabledException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                AuthUserResponse.from(user));
    }

    @Transactional
    public RegisterResponse registerCustomer(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPhone(normalizePhone(request.phone()));

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private RegisterResponse toResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }
}

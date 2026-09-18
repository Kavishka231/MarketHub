package com.markethub.user;
import org.springframework.data.jpa.repository.*; import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long>,JpaSpecificationExecutor<User>{Optional<User> findByEmail(String email);boolean existsByEmail(String email);long countByRole(UserRole role);long countByStatus(UserStatus status);}
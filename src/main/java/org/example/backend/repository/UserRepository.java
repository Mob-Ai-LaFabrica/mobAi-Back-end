package org.example.backend.repository;

import org.example.backend.entity.User;
import org.example.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.active = true")
    Optional<User> findByUsernameOrEmailAndActiveTrue(@Param("login") String login);

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    List<User> findByRole(Role role);

    List<User> findByActive(Boolean active);

    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) " +
            "AND (:active IS NULL OR u.active = :active)")
    Page<User> findWithFilters(
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countByRoleAndActiveTrue(@Param("role") Role role);
}

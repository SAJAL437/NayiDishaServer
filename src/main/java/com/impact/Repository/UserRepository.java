package com.impact.Repository;

import com.impact.data.domain.ERole;
import com.impact.entity.Role;
import com.impact.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByid(Long id);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    Page<User> findByRoles(Role role, Pageable pageable);

    boolean existsByRoles_Name(ERole role);
}

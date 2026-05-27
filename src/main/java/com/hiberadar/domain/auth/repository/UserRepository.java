package com.hiberadar.domain.auth.repository;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findByRoleAndProfileCompletedTrue(UserRole role);

    List<AppUser> findByRoleIn(List<UserRole> roles);
}

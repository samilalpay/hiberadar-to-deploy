package com.hiberadar.domain.auth.repository;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findByRoleAndProfileCompletedTrue(UserRole role);

    Page<AppUser> findByRole(UserRole role, Pageable pageable);

    Page<AppUser> findByRoleAndActive(UserRole role, Boolean active, Pageable pageable);

    @Query("select u from AppUser u where u.role = :role and (lower(u.companyName) like lower(concat('%', :q, '%')) or lower(u.username) like lower(concat('%', :q, '%')))")
    Page<AppUser> searchByRoleAndCompanyNameOrUsername(@Param("role") UserRole role, @Param("q") String q, Pageable pageable);

    @Query("select u from AppUser u where u.role = :role and u.active = :active and (lower(u.companyName) like lower(concat('%', :q, '%')) or lower(u.username) like lower(concat('%', :q, '%')))")
    Page<AppUser> searchByRoleAndActiveAndCompanyNameOrUsername(@Param("role") UserRole role, @Param("active") Boolean active, @Param("q") String q, Pageable pageable);

    List<AppUser> findByRoleIn(List<UserRole> roles);
}

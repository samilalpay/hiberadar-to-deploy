package com.hiberadar.common.security;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbUserDetailsService")
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Spring Security "ROLE_" prefix bekler (hasRole('ADMIN') -> ROLE_ADMIN)
        String role = (u.getRole() == null) ? "FIRMA" : u.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
            u.isActive(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}

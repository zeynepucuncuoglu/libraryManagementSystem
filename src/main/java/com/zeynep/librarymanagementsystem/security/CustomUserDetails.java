package com.zeynep.librarymanagementsystem.security;

import com.zeynep.librarymanagementsystem.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return roles as "ROLE_" prefix is expected by Spring Security
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can implement custom logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can implement custom logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can implement custom logic if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // You can implement custom logic if needed
    }

    public User getUser() {
        return user;
    }
}

package com.rtb.manageyourmoneybackend.user;

import com.rksdev.security.api.IdentifiableUser;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails, IdentifiableUser {

    private final UserEntity user;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor accepts the numeric ID from the database entity
    public CustomUserDetails(UserEntity user) {
        this.user = user;
        this.authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUserId() {
        return this.user.getId(); // Satisfies IdentifiableUser for the library
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.user.isEnabled();
    }
}

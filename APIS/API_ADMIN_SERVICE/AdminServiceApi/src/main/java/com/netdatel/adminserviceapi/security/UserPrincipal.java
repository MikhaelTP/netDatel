package com.netdatel.adminserviceapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {

    private Integer userId;
    private String email;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Integer userId, String email, String username, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.authorities = authorities;
    }

    // ✅ Método para el resolver
    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // No almacenamos password en el principal
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }
}
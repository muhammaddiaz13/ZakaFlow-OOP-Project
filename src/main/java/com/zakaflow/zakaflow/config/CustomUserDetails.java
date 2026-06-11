package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OidcUser {

    private final User user;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    // Constructor untuk Form Login biasa
    public CustomUserDetails(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
        this.idToken = null;
        this.userInfo = null;
    }

    // Constructor untuk Google OAuth2 (OIDC) Login
    public CustomUserDetails(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public User getUser() {
        return this.user;
    }

    // === Implementasi UserDetails ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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

    // === Implementasi OAuth2User ===
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    // === Implementasi OidcUser ===
    @Override
    public Map<String, Object> getClaims() {
        return idToken != null ? idToken.getClaims() : Collections.emptyMap();
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }
}

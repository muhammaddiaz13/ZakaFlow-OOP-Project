package com.zakaflow.zakaflow.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SimpleUrlAuthenticationSuccessHandler donaturHandler =
            new SimpleUrlAuthenticationSuccessHandler("/user");
    private final SimpleUrlAuthenticationSuccessHandler adminHandler =
            new SimpleUrlAuthenticationSuccessHandler("/admin");

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        boolean isDonatur = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DONATUR"::equals);
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isDonatur) {
            donaturHandler.onAuthenticationSuccess(request, response, authentication);
        } else if (isAdmin) {
            adminHandler.onAuthenticationSuccess(request, response, authentication);
        } else {
            new SimpleUrlAuthenticationSuccessHandler("/").onAuthenticationSuccess(request, response, authentication);
        }
    }
}

package com.zakaflow.zakaflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // SUDAH DIPERBAIKI: Menggunakan "/forgot-password/**" dengan garis miring di depan
                        .requestMatchers("/", "/programs/**", "/categories/**", "/login", "/register", "/forgot-password/**", "/error", "/css/**", "/js/**", "/img/**", "/uploads/**", "/image-upload/**").permitAll()
                        .requestMatchers("/user/**").hasRole("DONATUR")
                        .requestMatchers("/admin/**", "/transactions/**").hasRole("ADMIN")
                        .anyRequest().authenticated()) // Menggunakan authenticated() agar endpoint lain aman
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(new RoleBasedAuthenticationSuccessHandler())
                        .permitAll())
                // Konfigurasi ini menghubungkan Spring Security dengan library OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
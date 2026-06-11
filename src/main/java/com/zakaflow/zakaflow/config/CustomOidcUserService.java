package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.model.Role;
import com.zakaflow.zakaflow.model.User;
import com.zakaflow.zakaflow.repository.RoleRepository;
import com.zakaflow.zakaflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        
        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (Exception ex) {
            log.error("Gagal menyinkronkan akun Google dengan database lokal", ex);
            throw new OAuth2AuthenticationException("Gagal menyinkronkan akun Google: " + ex.getMessage());
        }
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email tidak ditemukan dari provider Google");
        }

        log.info("===[LOGIN OAUTH2]=== Memproses login Google untuk email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("===[LOGIN OAUTH2]=== User terdaftar ditemukan di database. ID: {}, Role: {}", user.getId(), user.getRole());
        } else {
            log.info("===[LOGIN OAUTH2]=== Email '{}' belum terdaftar. Mendaftarkan akun baru...", email);
            user = new User();
            user.setEmail(email);
            
            // Buat username unik berbasis email
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = baseUsername + counter;
                counter++;
            }
            user.setUsername(username);
            
            // Generate password acak aman
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole("DONATUR");

            Role roleEntity = roleRepository.findByName("DONATUR")
                    .orElseThrow(() -> new IllegalStateException("Role 'DONATUR' tidak ditemukan di database. Hubungi admin."));
            user.setRoleEntity(roleEntity);

            user = userRepository.save(user);
            log.info("===[LOGIN OAUTH2]=== Akun baru berhasil dibuat dengan ID: {}, Username: {}", user.getId(), user.getUsername());
        }

        return new CustomUserDetails(user, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}

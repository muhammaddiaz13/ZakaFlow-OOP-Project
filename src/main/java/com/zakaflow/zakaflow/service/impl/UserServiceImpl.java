package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.Role;
import com.zakaflow.zakaflow.model.User;
import com.zakaflow.zakaflow.repository.RoleRepository;
import com.zakaflow.zakaflow.repository.UserRepository;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User register(String username, String email, String rawPassword, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah digunakan");
        }

        String resolvedRoleName =
                (roleName != null && !roleName.isBlank()) ? roleName.trim().toUpperCase() : "DONATUR";

        Role roleEntity = roleRepository.findByName(resolvedRoleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Role '" + resolvedRoleName + "' belum ada di database. Hubungi administrator."));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
<<<<<<< HEAD
        user.setRole(resolvedRoleName);
        user.setRoleEntity(roleEntity);
=======
        String resolvedRoleName =
                (roleName != null && !roleName.isBlank()) ? roleName.trim().toUpperCase() : "DONATUR";
        user.setRole(resolvedRoleName);

        Role roleEntity = roleRepository.findByName(resolvedRoleName)
                .orElseGet(() -> roleRepository.save(new Role(null, resolvedRoleName)));
        user.setRoleEntity(roleEntity);

>>>>>>> origin
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerDonatur(String username, String email, String rawPassword) {
        return register(username, email, rawPassword, "DONATUR");
    }

    @Override
    @Transactional
    public User updateEmail(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        String trimmed = email.trim();
        userRepository.findByEmail(trimmed).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalArgumentException("Email sudah digunakan");
            }
        });
        user.setEmail(trimmed);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password lama tidak sesuai");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password baru minimal 6 karakter");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}

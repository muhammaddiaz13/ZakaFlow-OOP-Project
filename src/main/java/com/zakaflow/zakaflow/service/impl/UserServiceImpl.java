package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.model.Role;
import com.zakaflow.zakaflow.model.User;
import com.zakaflow.zakaflow.repository.UserRepository;
import com.zakaflow.zakaflow.service.RoleService;
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
    private final RoleService roleService;
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
    public User register(String username, String rawPassword, String roleName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role tidak ditemukan: " + roleName));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}

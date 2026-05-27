package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    User register(String username, String email, String rawPassword, String roleName);

    User registerDonatur(String username, String email, String rawPassword);

    User updateEmail(Long userId, String email);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void deleteById(Long id);
}

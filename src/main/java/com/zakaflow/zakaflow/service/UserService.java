package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    User register(String username, String rawPassword, String roleName);

    void deleteById(Long id);
}

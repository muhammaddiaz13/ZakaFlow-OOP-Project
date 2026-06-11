package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.zakaflow.zakaflow.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));
        return new CustomUserDetails(user);
    }
}

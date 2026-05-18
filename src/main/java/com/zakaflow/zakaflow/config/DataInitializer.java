package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.model.Role;
import com.zakaflow.zakaflow.repository.CategoryRepository;
import com.zakaflow.zakaflow.repository.RoleRepository;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    public void run(String... args) {
        seedRole("ADMIN");
        seedRole("DONATUR");

        seedCategory("Zakat", "Kewajiban zakat bagi yang memenuhi syarat");
        seedCategory("Infak", "Sedekah sunnah di luar zakat");
        seedCategory("Sedekah", "Bantuan sukarela untuk yang membutuhkan");

        if (userService.findByUsername("admin").isEmpty()) {
            userService.register("admin", "admin123", "ADMIN");
        }
        if (userService.findByUsername("donatur").isEmpty()) {
            userService.register("donatur", "donatur123", "DONATUR");
        }
    }

    private void seedRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(null, name, null));
        }
    }

    private void seedCategory(String name, String description) {
        if (categoryRepository.findByName(name).isEmpty()) {
            categoryRepository.save(new Category(null, name, description, null));
        }
    }
}

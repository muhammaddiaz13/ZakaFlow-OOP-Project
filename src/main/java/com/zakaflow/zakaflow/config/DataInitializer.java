package com.zakaflow.zakaflow.config;

import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.repository.CategoryRepository;
import com.zakaflow.zakaflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    public void run(String... args) {
        seedCategory("Zakat", "Kewajiban zakat bagi yang memenuhi syarat");
        seedCategory("Infak", "Sedekah sunnah di luar zakat");
        seedCategory("Sedekah", "Bantuan sukarela untuk yang membutuhkan");

        if (userService.findByUsername("admin").isEmpty()) {
            userService.register("admin", "admin@zakaflow.local", "admin123", "ADMIN");
        }
        if (userService.findByUsername("donatur").isEmpty()) {
            userService.register("donatur", "donatur@zakaflow.local", "donatur123", "DONATUR");
        }
    }

    private void seedCategory(String name, String description) {
        if (categoryRepository.findByName(name).isEmpty()) {
            categoryRepository.save(new Category(null, name, description, null));
        }
    }
}

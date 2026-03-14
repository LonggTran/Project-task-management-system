package com.projecttaskmanager.backend.config;

import com.projecttaskmanager.backend.models.Role;
import com.projecttaskmanager.backend.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(
                    Role.builder()
                            .name("USER")
                            .description("Default user role")
                            .build()
            );
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(
                    Role.builder()
                            .name("ADMIN")
                            .description("Administrator role")
                            .build()
            );
        }

        System.out.println("Roles seeded successfully");
    }
}
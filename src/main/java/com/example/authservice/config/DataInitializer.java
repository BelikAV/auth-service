package com.example.authservice.config;

import com.example.authservice.entity.Role;
import com.example.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initRole("ROLE_USER");
        initRole("ROLE_ADMIN");
    }


    private void initRole(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Создана роль: {}", roleName);
        } else {
            log.info("Роль уже существует: {}", roleName);
        }
    }

}

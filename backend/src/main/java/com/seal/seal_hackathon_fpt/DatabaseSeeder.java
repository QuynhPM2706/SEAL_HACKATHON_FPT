package com.seal.seal_hackathon_fpt;

import com.seal.seal_hackathon_fpt.features.user.entity.Role;
import com.seal.seal_hackathon_fpt.features.user.entity.User;
import com.seal.seal_hackathon_fpt.features.user.entity.UserStatus;
import com.seal.seal_hackathon_fpt.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seeder.admin.name}")
    private String adminName;

    @Value("${app.seeder.admin.email}")
    private String adminEmail;

    @Value("${app.seeder.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            System.out.println("=======> ADMIN ĐÃ TỒN TẠI, KHÔNG TẠO LẠI: " + adminEmail + " <=======");
            return;
        }

        User admin = User.builder()
                .full_name(adminName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.Admin)
                .status(UserStatus.active)
                .build();

        userRepository.save(admin);

        System.out.println("=======> ĐÃ TẠO ADMIN: " + adminEmail + " / " + adminPassword + " <=======");
    }
}
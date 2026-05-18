package kz.testmanagement.config;

import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultAdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner defaultAdminRunner() {
        return args -> {
            User admin = userRepository.findByEmail("admin@local")
                    .orElseGet(User::new);
            admin.setFullName("Administrator");
            admin.setEmail("admin@local");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);
            admin.setBlocked(false);
            userRepository.save(admin);
        };
    }
}

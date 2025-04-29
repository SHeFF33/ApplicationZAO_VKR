package ru.zaomurom.applicationzao.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.services.UserService;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setAdmin(true);
            admin.setName("Денис Шеварев");
            admin.setEmail("adminchik@gmail.com");
            userService.save(admin);
        }
    }
}

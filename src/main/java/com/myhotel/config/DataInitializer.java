package com.myhotel.config;

import com.myhotel.entity.User;
import com.myhotel.entity.enums.Gender;
import com.myhotel.entity.enums.Role;
import com.myhotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            // Create Admin user
            User admin = new User();
            admin.setEmail("admin@myhotel.com");
            admin.setPassword("admin123");
            admin.setName("Admin User");
            admin.setDateOfBirth(LocalDate.of(1990, 1, 1));
            admin.setGender(Gender.MALE);
            admin.setRoles(Set.of(Role.ADMIN));
            userRepository.save(admin);

            // Create Hotel Manager user
            User manager = new User();
            manager.setEmail("manager@myhotel.com");
            manager.setPassword("manager123");
            manager.setName("Hotel Manager");
            manager.setDateOfBirth(LocalDate.of(1985, 5, 15));
            manager.setGender(Gender.FEMALE);
            manager.setRoles(Set.of(Role.HOTEL_MANAGER));
            userRepository.save(manager);

            // Create Guest user
            User guest = new User();
            guest.setEmail("guest@myhotel.com");
            guest.setPassword("guest123");
            guest.setName("Guest User");
            guest.setDateOfBirth(LocalDate.of(1992, 8, 20));
            guest.setGender(Gender.OTHER);
            guest.setRoles(Set.of(Role.GUEST));
            userRepository.save(guest);

            log.info("Test users initialized successfully");
            log.info("Admin: admin@myhotel.com / admin123");
            log.info("Manager: manager@myhotel.com / manager123");
            log.info("Guest: guest@myhotel.com / guest123");
        }
    }
}

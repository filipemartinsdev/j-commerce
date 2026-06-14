package com.identity.config;

import com.identity.profile.domain.entity.UserProfile;
import com.identity.profile.infra.persistence.UserProfileRepository;
import com.identity.security.domain.entity.Role;
import com.identity.security.domain.entity.UserCredentials;
import com.identity.security.infra.persistence.RoleRepository;
import com.identity.security.infra.persistence.UserCredentialsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration @Profile("!test")
public class DatabaseSeeder {
    private final UserCredentialsRepository userCredentialsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserCredentialsRepository userCredentialsRepository, UserProfileRepository userProfileRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    @Transactional
    public CommandLineRunner seedDatabase() {
        return args -> {
            if (userCredentialsRepository.count() == 0) {
                log.info("Creating default users");

                createUser("common@gmail.com", "common123", "common", "test",
                        List.of(Role.Value.USER.getId())
                );

                createUser("admin@gmail.com", "admin123", "admin", "test",
                        List.of(Role.Value.USER.getId(), Role.Value.ADMIN.getId(), Role.Value.DRIVER.getId())
                );

                createUser("stockman@gmail.com", "stockman123", "stockman", "test",
                        List.of(Role.Value.USER.getId(), Role.Value.STOCK_MANAGER.getId())
                );

                createUser("logistics@gmail.com", "logistics123", "logistics", "test",
                        List.of(Role.Value.USER.getId(), Role.Value.LOGISTICS.getId())
                );

                createUser("driver@gmail.com", "driver123", "driver", "test",
                        List.of(Role.Value.USER.getId(), Role.Value.DRIVER.getId())
                );
            }
        };
    }

    private void createUser(String email, String password, String firstName, String lastName, List<Integer> roles) {
        var credentials = new UserCredentials();
        credentials.setEmail(email);
        credentials.setEncryptedPassword(passwordEncoder.encode(password));
        credentials.setFirstName(firstName);
        credentials.setLastName(lastName);
        credentials.setRoles(getRoleEntities(roles));

        var createdCredentials = userCredentialsRepository.save(credentials);

        var profile = new UserProfile();
        profile.setUserId(createdCredentials.getUserId());
        profile.setEmail(email);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);

        userProfileRepository.save(profile);

        log.info("Default user created: {}", credentials.getEmail());
    }

    private List<Role> getRoleEntities(List<Integer> idList) {
        List<Role> roles = new ArrayList<>();

        for (int id : idList){
            roles.add(roleRepository.getReferenceById(id));
        }

        return roles;
    }
}

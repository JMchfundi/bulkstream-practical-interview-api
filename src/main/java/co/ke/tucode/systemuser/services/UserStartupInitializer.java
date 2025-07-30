package co.ke.tucode.systemuser.services;

import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.entities.Role;
import co.ke.tucode.systemuser.repositories.Africana_UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserStartupInitializer {

    @Bean
    public CommandLineRunner createInitialUser(Africana_UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultEmail = "admin@tucode.co.ke";
            if (userRepository.findByEmail(defaultEmail).isEmpty()) {
                TRES_User user = TRES_User.builder()
                        .username("admin")
                        .email(defaultEmail)
                        .password(passwordEncoder.encode("Password@2906"))
                        .role(Role.ADMIN)
                        .user_signature("initial_signature")
                        .build();

                userRepository.save(user);
                System.out.println("✔ Default admin user created." + defaultEmail);
            } else {
                System.out.println("ℹ Default admin user already exists." + defaultEmail);
            }
            defaultEmail = "Jakida@tucode.co.ke";
            if (userRepository.findByEmail(defaultEmail).isEmpty()) {
                TRES_User user = TRES_User.builder()
                        .username("Jafari M. Akida")
                        .email(defaultEmail)
                        .password(passwordEncoder.encode("Passres@2906"))
                        .role(Role.ADMIN)
                        .user_signature("initial_signature")
                        .build();

                userRepository.save(user);
                System.out.println("✔ Default admin user created." + defaultEmail);
            }
            else {
                TRES_User user = userRepository.findByEmail(defaultEmail).get(0);
                user.setUsername("Jafari M. Akida");
                userRepository.save(user);
                System.out.println("ℹ Default admin user already exists." + defaultEmail);
            }
        };
    }
}

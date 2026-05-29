package com.minhthien.web.coach;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "COACH API",
                version = "1.0",
                description = "API documentation for COACH application"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER
)
@SpringBootApplication
public class CoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoachApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initAdmin(
            com.minhthien.web.coach.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                com.minhthien.web.coach.entity.User admin = com.minhthien.web.coach.entity.User.builder()
                        .username("admin")
                        .email("admin@coachfinder.com")
                        .fullName("System Admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(com.minhthien.web.coach.enums.UserRole.ADMIN)
                        .active(true)
                        .build();
                userRepository.save(admin);
                System.out.println("=========================================");
                System.out.println(" Admin account created: admin / admin123 ");
                System.out.println("=========================================");
            }
        };
    }

}

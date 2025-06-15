package com.impact;

import com.impact.entity.Role;
import com.impact.entity.User;
import com.impact.Repository.RoleRepository;
import com.impact.Repository.UserRepository;
import com.impact.data.domain.ERole;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableJpaAuditing
@SpringBootApplication
public class SocialImpactServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialImpactServerApplication.class, args);
	}

	@Bean
	CommandLineRunner run(RoleRepository roleRepository) {
		return args -> {
			if (!roleRepository.existsByName(ERole.ROLE_USER)) {
				roleRepository.save(Role.builder().name(ERole.ROLE_USER).build());
			}
			if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
				roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());
			}
		};
	}

	@Bean
	CommandLineRunner init(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
		return args -> {
			if (userRepo.findByEmail("admin@example.com").isEmpty()) {
				// Check if ROLE_ADMIN exists, if not create it
				Role adminRole = roleRepo.findByName(ERole.ROLE_ADMIN)
						.orElseGet(() -> roleRepo.save(new Role(ERole.ROLE_ADMIN)));

				// Create admin user
				User admin = new User();
				admin.setName("Admin");
				admin.setEmail("admin@example.com");
				admin.setPassword(encoder.encode("admin123"));
				admin.setRoles(Set.of(adminRole));
				admin.setPhoneNumber("+91 9555715453");
				admin.setBio("​कर्मण्येवाधिकारस्ते");
				admin.setEnabled(true);
				admin.setVerified(true);
				// admin.setEnabled(true); // Optional, if you use this flag

				userRepo.save(admin);
				System.out.println("✅ Admin user created");
			}
		};
	}

}

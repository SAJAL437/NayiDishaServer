package com.impact.services.impl;

import com.impact.Repository.ReportIssueRepository;
import com.impact.Repository.RoleRepository;
import com.impact.Repository.UserRepository;
import com.impact.configuration.JwtUtils;
import com.impact.data.domain.ERole;
import com.impact.entity.ReportIssue;
import com.impact.entity.Role;
import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.request.SignupRequest;
import com.impact.services.EmailServices;
import com.impact.services.UserServices;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServicesImpl implements UserServices {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReportIssueRepository reportIssueRepository;
    private final EmailServices emailServices;

    public UserServicesImpl(PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            UserRepository userRepository, RoleRepository roleRepository, ReportIssueRepository reportIssueRepository,
            EmailServices emailServices) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.reportIssueRepository = reportIssueRepository;
        this.emailServices = emailServices;
    }

    @Override
    public User findUserProfileByJwt(String jwt) throws UserException {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        String email = jwtUtils.extractUsername(jwt);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void registerUser(SignupRequest signupRequest) throws UserException {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new UserException("Email is already in use.");
        }
        User user = new User();
        user.setName(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEnabled(false); // Set to false until verified
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setBio(signupRequest.getBio());
        user.setCloudinaryImagePublicId(signupRequest.getCloudinaryImagePublicId());
        user.setPicture(signupRequest.getPicture());
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        Set<Role> userRoles = new HashSet<>();
        if (signupRequest.getRoles() == null || signupRequest.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new UserException("Role not found."));
            userRoles.add(defaultRole);
        } else {
            for (String roleStr : signupRequest.getRoles()) {
                ERole roleEnum = ERole.valueOf(roleStr);
                if (roleEnum == ERole.ROLE_ADMIN && userRepository.existsByRoles_Name(ERole.ROLE_ADMIN)) {
                    throw new UserException("Admin user already exists.");
                }
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new UserException("Role not found: " + roleStr));
                userRoles.add(role);
            }
        }

        user.setRoles(userRoles);
        userRepository.save(user);
        try {
            emailServices.sendVerificationEmail(signupRequest.getEmail(), user.getVerificationToken());
        } catch (Exception e) {
            throw new UserException("Failed to send verification email: " + e.getMessage());
        }
    }

    @Override
    public void verifyUser(String token) throws UserException {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UserException("Invalid verification token"));
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException("Verification token has expired");
        }
        user.setVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public User getUserbyid(Long id) throws UserException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found with ID: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User update(User userData, Long id) throws UserException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found with ID: " + id));

        existingUser.setName(userData.getName());
        existingUser.setEmail(userData.getEmail());
        existingUser.setPhoneNumber(userData.getPhoneNumber());
        existingUser.setBio(userData.getBio());

        if (userData.getPicture() != null)
            existingUser.setPicture(userData.getPicture());

        if (userData.getCloudinaryImagePublicId() != null)
            existingUser.setCloudinaryImagePublicId(userData.getCloudinaryImagePublicId());

        if (userData.getPassword() != null && !userData.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void delete(Long id) throws UserException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found with ID: " + id));
        userRepository.delete(user);
    }

    public Page<User> getAllUsersWithUserRole(Pageable pageable) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
        return userRepository.findByRoles(userRole, pageable);
    }

    @Override
    public User getUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

}

package com.impact.controller;

import com.impact.Form.UserForm;
import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.services.CloudinaryService;
import com.impact.services.UserServices;
import com.impact.services.impl.ReportIssueServicesImpl;
import com.impact.entity.ReportIssue;
import com.impact.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserServices userService;
    private final ReportIssueServicesImpl reportIssueServices;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public UserController(UserServices userService, UserRepository userRepository,
                          ReportIssueServicesImpl reportIssueServices, CloudinaryService cloudinaryService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.reportIssueServices = reportIssueServices;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/issues")
    public ResponseEntity<List<ReportIssue>> getLoggedInUserIssues(Authentication authentication) throws UserException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<ReportIssue> userIssues = reportIssueServices.getByUser(user);
        return ResponseEntity.ok(userIssues);
    }

    @PatchMapping(value = "/profile/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateCurrentUserProfile(@ModelAttribute UserForm userForm)
            throws UserException, IOException {
        try {
            // Get authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserException("User not found"));

            // Update fields
            user.setName(userForm.getName());
            user.setEmail(userForm.getEmail());
            user.setPhoneNumber(userForm.getPhoneNumber());
            user.setBio(userForm.getBio());
            if (userForm.getPassword() != null && !userForm.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userForm.getPassword()));
            }

            // Handle image upload
            if (userForm.getPicture() != null && !userForm.getPicture().isEmpty()) {
                System.out.println("Uploading picture: " + userForm.getPicture().getOriginalFilename());
                Map uploadResult = cloudinaryService.upload(userForm.getPicture());
                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();
                user.setPicture(imageUrl);
                user.setCloudinaryImagePublicId(publicId);
            }

            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace(); // Enhanced logging
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update profile: " + e.getMessage()));
        }
    }

    private User mapUserFormToUser(UserForm form) {
        return User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .password(form.getPassword())
                .phoneNumber(form.getPhoneNumber())
                .bio(form.getBio())
                .build(); // Remove picture and cloudinaryImagePublicId as they’re set in controller
    }

    // Inner class for error response
    private static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
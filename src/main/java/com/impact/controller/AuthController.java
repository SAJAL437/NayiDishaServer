package com.impact.controller;

import com.impact.LoginRequest;
import com.impact.Repository.UserRepository;
import com.impact.configuration.JwtUtils;
import com.impact.data.domain.ERole;
import com.impact.Repository.RoleRepository;
import com.impact.entity.Role;
import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.request.SignupRequest;
import com.impact.response.AuthResponse;
import com.impact.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UserServices userService;

    public AuthController(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            UserDetailsService userDetailsService,
            UserServices userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> createUserHandler(@Valid @RequestBody SignupRequest signupRequest)
            throws UserException {
        userService.registerUser(signupRequest);
        return new ResponseEntity<>("Registration successful. Please verify your email.", HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyUser(token);
            return ResponseEntity.ok("Email verified successfully. You can now login.");
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.getUserByEmail(loginRequest.getEmail());
            if (!user.isVerified()) {
                throw new UserException("Email not verified. Please verify your email first.");
            }
            Authentication authentication = authenticate(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtils.generateToken(authentication);
            return new ResponseEntity<>(new AuthResponse(token, true), HttpStatus.OK);
        } catch (UserException | BadCredentialsException e) {
            return new ResponseEntity<>(new AuthResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
        }
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
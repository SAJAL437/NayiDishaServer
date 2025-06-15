package com.impact.Helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.impact.configuration.CustomUserDetails;

public class AuthUtils {

    public static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("No authentication data found in SecurityContext");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        } else {
            throw new RuntimeException(
                    "Principal is not instance of CustomUserDetails. Actual type: " + principal.getClass());
        }
    }

    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // typically the email
        } else {
            return principal.toString();
        }
    }
}

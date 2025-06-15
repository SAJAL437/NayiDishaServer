package com.impact.Form;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserForm {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String bio;
    private MultipartFile picture; // Unified picture as MultipartFile
}
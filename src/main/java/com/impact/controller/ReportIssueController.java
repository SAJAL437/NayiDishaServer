package com.impact.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
// other imports...
import org.springframework.web.bind.annotation.RestController;

import com.impact.Form.ReportIssueForm;
import com.impact.Repository.UserRepository;
import com.impact.configuration.CustomUserDetails;
import com.impact.entity.ReportIssue;
import com.impact.entity.User;
import com.impact.services.IssueServices;

@RestController
@RequestMapping("/api/users/reportIssue")
public class ReportIssueController {

    private final IssueServices issueServices;
    private final UserRepository userRepository; // Add this field and inject it

    public ReportIssueController(IssueServices issueServices, UserRepository userRepository) {
        this.issueServices = issueServices;
        this.userRepository = userRepository;
    }

    @PostMapping("/")
    public ResponseEntity<ReportIssue> addReportIssue(
            @ModelAttribute ReportIssueForm form,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ReportIssue saved = issueServices.create(form, user);

        return ResponseEntity.ok(convertToDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportIssue> getReportIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            ReportIssue issue = issueServices.getById(id);
            if (!issue.getUser().getId().equals(userDetails.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(convertToDto(issue));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private ReportIssue convertToDto(ReportIssue issue) {
        return ReportIssue.builder()
                .id(issue.getId())
                .name(issue.getName())
                .email(issue.getEmail())
                .phoneNumber(issue.getPhoneNumber())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .location(issue.getLocation())
                .address(issue.getAddress())
                .status(issue.getStatus())
                .picture(issue.getPicture())
                .cloudinaryImagePublicId(issue.getCloudinaryImagePublicId())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}

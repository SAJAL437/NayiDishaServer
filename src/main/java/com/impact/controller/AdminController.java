package com.impact.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.impact.Form.ReportIssueForm;
import com.impact.configuration.CustomUserDetails;
import com.impact.dto.ReportIssueDto;
import com.impact.entity.ReportIssue;
import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.services.IssueServices;
import com.impact.services.UserServices;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    private final IssueServices issueServices;
    private final UserServices userService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public AdminController(IssueServices issueServices, UserServices userService) {
        this.issueServices = issueServices;
        this.userService = userService;
    }

    // -------------------- ADMIN PROFILE --------------------

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // -------------------- ISSUE MANAGEMENT --------------------

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<ReportIssueDto>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        log.info("Received request for get-all-issues: page={}, size={}, sortBy={}", page, size, sortBy);

        try {
            String[] validFields = { "id", "title", "createdAt" };
            if (!Arrays.asList(validFields).contains(sortBy)) {
                throw new IllegalArgumentException("Invalid sort field: " + sortBy);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            Page<ReportIssueDto> issues = issueServices.getAllIssues(pageable);
            log.info("Successfully fetched {} issues", issues.getTotalElements());
            return ResponseEntity.ok(issues);

        } catch (IllegalArgumentException e) {
            log.error("User error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Server error fetching issues: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch issues: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/issues")
    public ResponseEntity<Page<ReportIssueDto>> filterIssues(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Pageable pageable) {

        Page<ReportIssueDto> result = issueServices.filterAndSearchIssues(
                category, status, userEmail, location, search, fromDate, toDate, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/issues/{id}/in-progress")
    public ResponseEntity<ReportIssueDto> setIssueInProgress(@PathVariable Long id) throws UserException {
        log.info("Received request to set issue {} to IN_PROGRESS", id);
        try {
            ReportIssueDto updatedIssue = issueServices.inProgressComplain(id);
            log.info("Successfully set issue {} to IN_PROGRESS", id);
            return ResponseEntity.ok(updatedIssue);
        } catch (UserException e) {
            log.error("User error updating issue status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Server error updating issue status: {}", e.getMessage());
            throw new RuntimeException("Failed to update issue status: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/issues/{id}/reject")
    public ResponseEntity<ReportIssueDto> rejectIssue(@PathVariable Long id) throws UserException {
        log.info("Received request to reject issue {}", id);
        try {
            ReportIssueDto updatedIssue = issueServices.rejectComplain(id);
            log.info("Successfully rejected issue {}", id);
            return ResponseEntity.ok(updatedIssue);
        } catch (UserException e) {
            log.error("User error rejecting issue: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Server error rejecting issue: {}", e.getMessage());
            throw new RuntimeException("Failed to reject issue: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/issues/{id}/resolve")
    public ResponseEntity<ReportIssueDto> resolveIssue(@PathVariable Long id) throws UserException {
        log.info("Received request to resolve issue {}", id);
        try {
            ReportIssueDto updatedIssue = issueServices.resolveComplain(id);
            log.info("Successfully resolved issue {}", id);
            return ResponseEntity.ok(updatedIssue);
        } catch (UserException e) {
            log.error("User error resolving issue: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Server error resolving issue: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve issue: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("view-issue/{id}")
    public ResponseEntity<ReportIssueDto> viewIssueDetails(@PathVariable Long id) throws UserException {
        ReportIssueDto dto = issueServices.viewisssue(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/issues/{id}")
    public ResponseEntity<ReportIssueDto> deleteIssue(@PathVariable Long id) throws UserException {
        ReportIssueDto dto = issueServices.deleteComplain(id);
        return ResponseEntity.ok(dto);
    }

    // -------------------- USER MANAGEMENT --------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all-users")
    public ResponseEntity<Page<User>> getAllUsersWithUserRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) throws UserException {

        log.info("Received request for get-all-users: page={}, size={}, sortBy={}", page, size, sortBy);

        try {
            String[] validFields = { "id", "name", "email", "createdAt" };
            if (!Arrays.asList(validFields).contains(sortBy)) {
                throw new UserException("Invalid sort field: " + sortBy);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            Page<User> users = userService.getAllUsersWithUserRole(pageable);
            log.info("Successfully fetched {} users", users.getTotalElements());
            return ResponseEntity.ok(users);

        } catch (UserException e) {
            log.error("User error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Server error fetching users: {}", e.getMessage());
            throw new UserException("Failed to fetch users: " + e.getMessage());
        }
    }

    // -------------------- HELPER METHODS --------------------

    private ReportIssue convertToDto(ReportIssue issue) {
        return ReportIssue.builder()
                .id(issue.getId())
                .name(issue.getUser().getName())
                .email(issue.getUser().getEmail())
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

    // -------------------- INNER CLASSES --------------------

    static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

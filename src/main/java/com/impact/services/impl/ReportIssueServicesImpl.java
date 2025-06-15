package com.impact.services.impl;

import com.impact.Form.ReportIssueForm;
import com.impact.Helper.ResourceNotFoundException;
import com.impact.Repository.ReportIssueRepository;
import com.impact.Repository.UserRepository;
import com.impact.data.domain.Status;
import com.impact.dto.ReportIssueDto;
import com.impact.entity.ReportIssue;
import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.services.CloudinaryService;
import com.impact.services.EmailServices;
import com.impact.services.IssueServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReportIssueServicesImpl implements IssueServices {
    private final ReportIssueRepository reportIssueRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final EmailServices emailService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ReportIssueServicesImpl(
            ReportIssueRepository reportIssueRepository,
            CloudinaryService cloudinaryService,
            UserRepository userRepository,
            EmailServices emailService) {
        this.reportIssueRepository = reportIssueRepository;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public ReportIssue create(ReportIssueForm form, User user) {
        ReportIssue issue = new ReportIssue();
        issue.setName(form.getName());
        issue.setEmail(form.getEmail());
        issue.setPhoneNumber(form.getPhoneNumber());
        issue.setTitle(form.getTitle());
        issue.setDescription(form.getDescription());
        issue.setLocation(form.getLocation());
        issue.setAddress(form.getAddress());
        issue.setCreatedAt(LocalDateTime.now());
        issue.setStatus(Status.PENDING);
        issue.setUser(user);

        if (form.getReportImage() != null && !form.getReportImage().isEmpty()) {
            try {
                Map result = cloudinaryService.upload(form.getReportImage());
                issue.setPicture(result.get("secure_url").toString());
                issue.setCloudinaryImagePublicId(result.get("public_id").toString());
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        ReportIssue savedIssue = reportIssueRepository.save(issue);

        try {
            emailService.sendComplaintConfirmationEmail(form.getEmail(), savedIssue);
        } catch (Exception e) {
            log.error("Failed to send confirmation email for issue ID {}: {}", savedIssue.getId(), e.getMessage(), e);
        }

        return savedIssue;
    }

    @Override
    public ReportIssue save(ReportIssue issue) {
        return reportIssueRepository.save(issue);
    }

    @Override
    public ReportIssue update(ReportIssue issue) {
        issue.setUpdatedAt(LocalDateTime.now());
        return reportIssueRepository.save(issue);
    }

    @Override
    public ReportIssue getById(Long id) {
        return reportIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
    }

    @Override
    public void delete(Long id) {
        ReportIssue issue = getById(id);
        reportIssueRepository.delete(issue);
    }

    @Override
    public List<ReportIssue> getByUser(User user) {
        return reportIssueRepository.findByUser(user);
    }

    @Override
    public Page<ReportIssueDto> getAllIssues(Pageable pageable) {
        log.info("Fetching all issues with pageable: {}", pageable);
        Page<ReportIssue> issues = reportIssueRepository.findAll(pageable);
        List<ReportIssueDto> dtos = issues.getContent().stream()
                .map(ReportIssueDto::new)
                .toList();
        log.info("Fetched {} issues", issues.getTotalElements());
        return new PageImpl<>(dtos, pageable, issues.getTotalElements());
    }

    @Override
    public Page<ReportIssueDto> getAllIssuesByuserEmail(String email, Pageable pageable) throws UserException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
        log.info("Fetching issues for user email: {}", email);
        Page<ReportIssue> issues = reportIssueRepository.findByUser(user, pageable);
        List<ReportIssueDto> dtos = issues.getContent().stream()
                .map(ReportIssueDto::new)
                .toList();
        log.info("Fetched {} issues for user", issues.getTotalElements());
        return new PageImpl<>(dtos, pageable, issues.getTotalElements());
    }

    @Override
    public ReportIssueDto inProgressComplain(Long id) throws UserException {
        ReportIssue issue = getById(id);
        if (issue.getStatus() == Status.INPROGRESS) {
            throw new UserException("Issue is already in progress");
        }
        if (issue.getStatus() == Status.RESOLVED || issue.getStatus() == Status.REJECTED) {
            throw new UserException("Cannot set issue to in-progress from " + issue.getStatus());
        }
        issue.setStatus(Status.INPROGRESS);
        issue.setUpdatedAt(LocalDateTime.now());
        ReportIssue updatedIssue = reportIssueRepository.save(issue);

        try {
            emailService.sendStatusUpdateEmail(issue.getEmail(), updatedIssue);
        } catch (Exception e) {
            log.error("Failed to send status update email for issue ID {}: {}", id, e.getMessage(), e);
        }

        return new ReportIssueDto(updatedIssue);
    }

    @Override
    public ReportIssueDto resolveComplain(Long id) throws UserException {
        ReportIssue issue = getById(id);
        if (issue.getStatus() == Status.RESOLVED) {
            throw new UserException("Issue is already resolved");
        }
        if (issue.getStatus() == Status.REJECTED) {
            throw new UserException("Cannot resolve a rejected issue");
        }
        issue.setStatus(Status.RESOLVED);
        issue.setUpdatedAt(LocalDateTime.now());
        ReportIssue updatedIssue = reportIssueRepository.save(issue);

        try {
            emailService.sendStatusUpdateEmail(issue.getEmail(), updatedIssue);
        } catch (Exception e) {
            log.error("Failed to send status update email for issue ID {}: {}", id, e.getMessage(), e);
        }

        return new ReportIssueDto(updatedIssue);
    }

    @Override
    public ReportIssueDto rejectComplain(Long id) throws UserException {
        ReportIssue issue = getById(id);
        if (issue.getStatus() == Status.REJECTED) {
            throw new UserException("Issue is already rejected");
        }
        if (issue.getStatus() == Status.RESOLVED) {
            throw new UserException("Cannot reject a resolved issue");
        }
        issue.setStatus(Status.REJECTED);
        issue.setUpdatedAt(LocalDateTime.now());
        ReportIssue updatedIssue = reportIssueRepository.save(issue);

        try {
            emailService.sendStatusUpdateEmail(issue.getEmail(), updatedIssue);
        } catch (Exception e) {
            log.error("Failed to send status update email for issue ID {}: {}", id, e.getMessage(), e);
        }

        return new ReportIssueDto(updatedIssue);
    }

    @Transactional
    @Override
    public ReportIssueDto deleteComplain(Long id) throws UserException {
        // Retrieve the issue
        ReportIssue issue = getById(id);

        // Create DTO before deletion
        ReportIssueDto issueDto = new ReportIssueDto(issue);
        ReportIssue updatedIssue = reportIssueRepository.save(issue);
        // Send email notification (optional)
        try {
            emailService.sendDeletionNotificationEmail(
                    issue.getEmail(),
                    issue,
                    "Your complaint (ID: " + id + ") has been deleted.");

            // "Your complaint (ID: " + id + ") has been deleted."
        } catch (Exception e) {
            log.error("Failed to send deletion notification email for issue ID {}: {}", id, e.getMessage(), e);
        }

        // Delete the issue
        try {
            reportIssueRepository.delete(issue);
            log.info("Successfully deleted issue with ID {}", id);
        } catch (Exception e) {
            log.error("Failed to delete issue with ID {}: {}", id, e.getMessage(), e);
            throw new UserException("Failed to delete issue: " + e.getMessage());
        }

        return issueDto;
    }

    @Override
    public Page<ReportIssueDto> filterAndSearchIssues(
            String category,
            String status,
            String userEmail,
            String location,
            String search,
            String fromDate,
            String toDate,
            Pageable pageable) {

        Specification<ReportIssue> spec = Specification.where(null);

        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category.trim()));
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                Status enumStatus = Status.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), enumStatus));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        if (userEmail != null && !userEmail.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("email"), userEmail.trim()));
        }

        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("location")),
                    "%" + location.toLowerCase().trim() + "%"));
        }

        if (search != null && !search.trim().isEmpty()) {
            String keyword = "%" + search.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), keyword),
                    cb.like(cb.lower(root.get("description")), keyword)));
        }

        // Parse dates safely
        try {
            if (fromDate != null && toDate != null && !fromDate.isEmpty() && !toDate.isEmpty()) {
                LocalDate from = LocalDate.parse(fromDate);
                LocalDate to = LocalDate.parse(toDate);
                spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt").as(LocalDate.class), from, to));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected format is yyyy-MM-dd");
        }

        Page<ReportIssue> page = reportIssueRepository.findAll(spec, pageable);
        return page.map(ReportIssueDto::new); // or use your own `toDto()` if needed
    }

    @Override
    public ReportIssueDto viewisssue(Long id) throws UserException {
        ReportIssue issue = reportIssueRepository.findById(id)
                .orElseThrow(() -> new UserException("Issue not found with ID: " + id));
        return convertToDto(issue);
    }

    private ReportIssueDto convertToDto(ReportIssue issue) {
        ReportIssueDto dto = new ReportIssueDto();
        dto.setId(issue.getId());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setStatus(issue.getStatus());
        dto.setName(issue.getUser().getName());
        dto.setEmail(issue.getUser().getEmail());
        dto.setLocation(issue.getLocation());
        dto.setAddress(issue.getAddress());
        dto.setCreatedAt(issue.getCreatedAt());
        dto.setPicture(issue.getPicture());
        return dto;
    }



}
package com.impact.dto;

import com.impact.data.domain.Status;
import com.impact.entity.ReportIssue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReportIssueDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String title;
    private String description;
    private String location;
    private String address;
    private String picture;
    private String cloudinaryImagePublicId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId; // Or UserDto if needed

    // Getters, setters, constructor
    public ReportIssueDto() {
    }

    public ReportIssueDto(ReportIssue issue) {
        this.id = issue.getId();
        this.name = issue.getName();
        this.email = issue.getEmail();
        this.phoneNumber = issue.getPhoneNumber();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.location = issue.getLocation();
        this.address = issue.getAddress();
        this.picture = issue.getPicture();
        this.cloudinaryImagePublicId = issue.getCloudinaryImagePublicId();
        this.status = issue.getStatus();
        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getUpdatedAt();
        this.userId = issue.getUser() != null ? issue.getUser().getId() : null;
    }
}
package com.impact.Form;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportIssueForm {

    private String name;
    private String email;
    private String phoneNumber;
    private String title;
    private String description;
    private String location;
    private String address;
    private String cloudinaryImagePublicId;
    private String picture;
    private MultipartFile reportImage;
}
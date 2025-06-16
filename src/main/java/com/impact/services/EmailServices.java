package com.impact.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.impact.entity.ReportIssue;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServices {
    private static final Logger logger = LoggerFactory.getLogger(EmailServices.class);
    private final JavaMailSender mailSender;

    public EmailServices(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        logger.info("Sending verification email to: {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Verify Your Email");

        String verificationLink = baseUrl + "/auth/verify?token=" + token; // ✅ use injected base URL

        helper.setText("<h1>Email Verification</h1><p>Click the link below to verify your email:</p>" +
                "<a href=\"" + verificationLink + "\">Verify Email</a>", true);

        mailSender.send(message);
        logger.info("Verification email sent to: {}", to);
    }

    public void sendComplaintConfirmationEmail(String to, ReportIssue issue) {
        logger.info("Sending complaint confirmation email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Complaint Registered - ID: " + issue.getId());

            // HTML content for complaint details
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("<h1>Complaint Registered Successfully</h1>");
            emailContent.append("<p>Dear ").append(issue.getName()).append(",</p>");
            emailContent.append("<p>Your complaint has been registered with the following details:</p>");
            emailContent.append("<table style='border-collapse: collapse; width: 100%;'>");
            emailContent.append(
                    "<tr><th style='border: 1px solid #ddd; padding: 8px; background-color: #f2f2f2;'>Field</th>");
            emailContent.append(
                    "<th style='border: 1px solid #ddd; padding: 8px; background-color: #f2f2f2;'>Details</th></tr>");
            emailContent.append(
                    "<tr><td style='border: 1px solid #ddd; padding: 8px;'>Complaint ID</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getId()).append("</td></tr>");
            emailContent.append(
                    "<tr><td style='border: 1px solid #ddd; padding: 8px;'>Title</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getTitle()).append("</td></tr>");
            emailContent.append(
                    "<tr><td style='border: 1px solid #ddd; padding: 8px;'>Description</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getDescription()).append("</td></tr>");
            emailContent.append(
                    "<tr><td style='border: 1px solid #ddd; padding: 8px;'>Status</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getStatus()).append("</td></tr>");
            emailContent.append(
                    "<tr><td style='border: 1px solid #ddd; padding: 8px;'>Submitted On</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getCreatedAt()).append("</td></tr>");

            emailContent.append("</table>");
            emailContent.append("<p>Thank you for reporting the issue. We will address it promptly.</p>");
            emailContent.append("<p>Best regards,<br>Impact Team</p>");

            helper.setText(emailContent.toString(), true);
            mailSender.send(message);
            logger.info("Complaint confirmation email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send complaint confirmation email to {}: {}", to, e.getMessage(), e);
            // Do not throw exception to avoid blocking complaint submission
        }
    }

    public void sendStatusUpdateEmail(String to, ReportIssue issue) {
        logger.info("Sending status update email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Complaint Status Updated - ID: " + issue.getId());
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("<h1>Complaint Status Updated</h1>")
                    .append("<p>Dear ").append(issue.getName()).append(",</p>")
                    .append("<p>The status of your complaint has been updated:</p>")
                    .append("<table style='border-collapse: collapse; width: 100%;'>")
                    .append("<tr><th style='border: 1px solid #ddd; padding: 8px;'>Field</th>")
                    .append("<th style='border: 1px solid #ddd; padding: 8px;'>Details</th></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Complaint ID</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getId()).append("</td></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Title</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getTitle()).append("</td></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>New Status</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getStatus()).append("</td></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Updated On</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getUpdatedAt()).append("</td></tr>")
                    .append("</table>")
                    .append("<p>Please contact us if you have any questions.</p>")
                    .append("<p>Best regards,<br />Impact Team</p>");
            helper.setText(emailContent.toString(), true);
            mailSender.send(message);
            logger.info("Status update email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send status update email to {}: {}", to, e.getMessage(), e);
        }
    }

    public void sendDeletionNotificationEmail(String to, ReportIssue issue, String customMessage) {
        logger.info("Sending deletion notification email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Complaint Deleted - ID: " + issue.getId());
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("<h1>Complaint Deleted</h1>")
                    .append("<p>Dear ").append(issue.getName()).append(",</p>")
                    .append("<p>").append(customMessage).append("</p>")
                    .append("<table style='border-collapse: collapse; width: 100%;'>")
                    .append("<tr><th style='border: 1px solid #ddd; padding: 8px;'>Field</th>")
                    .append("<th style='border: 1px solid #ddd; padding: 8px;'>Details</th></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Complaint ID</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getId()).append("</td></tr>")
                    .append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Title</td><td style='border: 1px solid #ddd; padding: 8px;'>")
                    .append(issue.getTitle()).append("</td></tr>")
                    .append("</table>")
                    .append("<p>Please contact us if you have any questions.</p>")
                    .append("<p>Best regards,<br />Impact Team</p>");
            helper.setText(emailContent.toString(), true);
            mailSender.send(message);
            logger.info("Deletion notification email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send deletion notification email to {}: {}", to, e.getMessage(), e);
        }
    }

}
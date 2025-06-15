package com.impact.services;

import com.impact.Form.ReportIssueForm;
import com.impact.dto.ReportIssueDto;
import com.impact.entity.ReportIssue;
import com.impact.entity.User;
import com.impact.exception.UserException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IssueServices {
    // Existing methods...
    ReportIssue create(ReportIssueForm form, User user);

    ReportIssue save(ReportIssue issue);

    ReportIssue update(ReportIssue issue);

    ReportIssue getById(Long id);

    void delete(Long id);

    List<ReportIssue> getByUser(User user);

    Page<ReportIssueDto> getAllIssues(Pageable pageable);

    Page<ReportIssueDto> getAllIssuesByuserEmail(String email, Pageable pageable) throws UserException;

    ReportIssueDto inProgressComplain(Long id) throws UserException;

    ReportIssueDto resolveComplain(Long id) throws UserException;

    ReportIssueDto rejectComplain(Long id) throws UserException;

    ReportIssueDto viewisssue(Long id) throws UserException;

    ReportIssueDto deleteComplain(Long id) throws UserException;

    Page<ReportIssueDto> filterAndSearchIssues(
            String category, String status, String userEmail, String location,
            String search, String fromDate, String toDate, Pageable pageable);


}
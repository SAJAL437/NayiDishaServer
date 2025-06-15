package com.impact.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.impact.data.domain.Status;
import com.impact.entity.ReportIssue;
import com.impact.entity.User;

public interface ReportIssueRepository extends JpaRepository<ReportIssue, Long>, JpaSpecificationExecutor<ReportIssue> {

    List<ReportIssue> findByUserId(Long userId);

    List<ReportIssue> findByUser(User user);

    Page<ReportIssue> findByUser(User user, Pageable pageable);

}

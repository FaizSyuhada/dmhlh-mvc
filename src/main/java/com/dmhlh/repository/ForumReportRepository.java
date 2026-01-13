package com.dmhlh.repository;

import com.dmhlh.entity.ForumReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {
    
    List<ForumReport> findByStatusOrderByCreatedAtDesc(ForumReport.Status status);
    
    List<ForumReport> findByStatusInOrderByCreatedAtDesc(List<ForumReport.Status> statuses);
    
    @Query("SELECT COUNT(r) FROM ForumReport r WHERE r.status = 'PENDING'")
    long countPendingReports();
    
    @Query("SELECT COUNT(r) FROM ForumReport r WHERE r.status IN ('PENDING', 'UNDER_REVIEW')")
    long countOpenReports();
}

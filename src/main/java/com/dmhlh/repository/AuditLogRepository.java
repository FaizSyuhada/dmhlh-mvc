package com.dmhlh.repository;

import com.dmhlh.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);
    
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(AuditLog.ActionType actionType);
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<AuditLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);
}

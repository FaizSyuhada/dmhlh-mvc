package com.dmhlh.repository;

import com.dmhlh.entity.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {
    
    List<LearningModule> findByStatusOrderByDisplayOrderAsc(LearningModule.Status status);
    
    @Query("SELECT m FROM LearningModule m ORDER BY m.displayOrder ASC")
    List<LearningModule> findAllOrderByDisplayOrder();
    
    long countByStatus(LearningModule.Status status);
}

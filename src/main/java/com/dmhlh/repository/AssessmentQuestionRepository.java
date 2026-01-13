package com.dmhlh.repository;

import com.dmhlh.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    
    List<AssessmentQuestion> findByDefinitionIdOrderByDisplayOrderAsc(Long definitionId);
}

package com.dmhlh.repository;

import com.dmhlh.entity.AssessmentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentDefinitionRepository extends JpaRepository<AssessmentDefinition, Long> {
    
    Optional<AssessmentDefinition> findByCode(String code);
    
    List<AssessmentDefinition> findByActiveTrue();
}

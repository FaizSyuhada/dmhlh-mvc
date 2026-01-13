package com.dmhlh.service;

import com.dmhlh.entity.LearningModule;
import com.dmhlh.entity.QuizQuestion;
import com.dmhlh.entity.User;
import com.dmhlh.repository.LearningModuleRepository;
import com.dmhlh.repository.QuizQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LearningModuleService {
    
    private final LearningModuleRepository moduleRepository;
    private final QuizQuestionRepository questionRepository;
    
    public LearningModuleService(LearningModuleRepository moduleRepository,
                                 QuizQuestionRepository questionRepository) {
        this.moduleRepository = moduleRepository;
        this.questionRepository = questionRepository;
    }
    
    public List<LearningModule> findPublished() {
        return moduleRepository.findByStatusOrderByDisplayOrderAsc(LearningModule.Status.PUBLISHED);
    }
    
    public List<LearningModule> findAll() {
        return moduleRepository.findAllOrderByDisplayOrder();
    }
    
    public Optional<LearningModule> findById(Long id) {
        return moduleRepository.findById(id);
    }
    
    public List<QuizQuestion> getQuizQuestions(Long moduleId) {
        return questionRepository.findByModuleIdOrderByDisplayOrderAsc(moduleId);
    }
    
    public boolean hasQuiz(Long moduleId) {
        return questionRepository.countByModuleId(moduleId) > 0;
    }
    
    @Transactional
    public LearningModule create(String title, String description, LearningModule.ModuleType type,
                                  String bodyContent, String videoUrl, LearningModule.Status status,
                                  Integer displayOrder, User createdBy) {
        LearningModule module = LearningModule.builder()
            .title(title)
            .description(description)
            .type(type)
            .bodyContent(bodyContent)
            .videoUrl(videoUrl)
            .status(status)
            .displayOrder(displayOrder)
            .createdBy(createdBy)
            .build();
        
        return moduleRepository.save(module);
    }
    
    @Transactional
    public LearningModule update(Long id, String title, String description, LearningModule.ModuleType type,
                                  String bodyContent, String videoUrl, LearningModule.Status status,
                                  Integer displayOrder) {
        LearningModule module = moduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        module.setTitle(title);
        module.setDescription(description);
        module.setType(type);
        module.setBodyContent(bodyContent);
        module.setVideoUrl(videoUrl);
        module.setStatus(status);
        module.setDisplayOrder(displayOrder);
        
        return moduleRepository.save(module);
    }
    
    @Transactional
    public void delete(Long id) {
        moduleRepository.deleteById(id);
    }
    
    @Transactional
    public QuizQuestion addQuestion(Long moduleId, String question, String optionA, String optionB,
                                    String optionC, String optionD, Character correctOption,
                                    String explanation, Integer displayOrder) {
        LearningModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        QuizQuestion q = QuizQuestion.builder()
            .module(module)
            .question(question)
            .optionA(optionA)
            .optionB(optionB)
            .optionC(optionC)
            .optionD(optionD)
            .correctOption(correctOption)
            .explanation(explanation)
            .displayOrder(displayOrder)
            .build();
        
        return questionRepository.save(q);
    }
    
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }
    
    public long countPublished() {
        return moduleRepository.countByStatus(LearningModule.Status.PUBLISHED);
    }
}

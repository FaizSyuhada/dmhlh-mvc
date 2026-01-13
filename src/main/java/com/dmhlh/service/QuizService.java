package com.dmhlh.service;

import com.dmhlh.entity.LearningModule;
import com.dmhlh.entity.QuizAttempt;
import com.dmhlh.entity.QuizQuestion;
import com.dmhlh.entity.User;
import com.dmhlh.repository.LearningModuleRepository;
import com.dmhlh.repository.QuizAttemptRepository;
import com.dmhlh.repository.QuizQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {
    
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final LearningModuleRepository moduleRepository;
    private final ObjectMapper objectMapper;
    
    public QuizService(QuizQuestionRepository questionRepository,
                       QuizAttemptRepository attemptRepository,
                       LearningModuleRepository moduleRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.moduleRepository = moduleRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    public List<QuizQuestion> getQuestions(Long moduleId) {
        return questionRepository.findByModuleIdOrderByDisplayOrderAsc(moduleId);
    }
    
    public Optional<QuizAttempt> getLatestAttempt(Long userId, Long moduleId) {
        return attemptRepository.findFirstByUserIdAndModuleIdOrderByCompletedAtDesc(userId, moduleId);
    }
    
    public List<QuizAttempt> getUserAttempts(Long userId) {
        return attemptRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }
    
    @Transactional
    public QuizAttempt submitQuiz(Long userId, Long moduleId, Map<Long, Character> answers) {
        User user = new User();
        user.setId(userId);
        
        LearningModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        List<QuizQuestion> questions = questionRepository.findByModuleIdOrderByDisplayOrderAsc(moduleId);
        
        int totalQuestions = questions.size();
        int correctAnswers = 0;
        
        for (QuizQuestion question : questions) {
            Character selected = answers.get(question.getId());
            if (selected != null && selected.equals(question.getCorrectOption())) {
                correctAnswers++;
            }
        }
        
        double scorePercentage = totalQuestions > 0 
            ? (correctAnswers * 100.0 / totalQuestions) 
            : 0;
        
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            answersJson = "{}";
        }
        
        QuizAttempt attempt = QuizAttempt.builder()
            .user(user)
            .module(module)
            .totalQuestions(totalQuestions)
            .correctAnswers(correctAnswers)
            .scorePercentage(scorePercentage)
            .answersJson(answersJson)
            .build();
        
        return attemptRepository.save(attempt);
    }
}

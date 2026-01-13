package com.dmhlh.controller;

import com.dmhlh.dto.StudentDashboardData;
import com.dmhlh.entity.*;
import com.dmhlh.facade.StudentDashboardFacade;
import com.dmhlh.listener.AssessmentEventListener;
import com.dmhlh.repository.CounsellorProfileRepository;
import com.dmhlh.repository.SessionTypeRepository;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.*;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {
    
    private final StudentDashboardFacade dashboardFacade;
    private final LearningModuleService moduleService;
    private final QuizService quizService;
    private final AssessmentService assessmentService;
    private final MoodLogService moodLogService;
    private final AppointmentService appointmentService;
    private final AiCoachService aiCoachService;
    private final CarePlanService carePlanService;
    private final NudgeService nudgeService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final CounsellorProfileRepository counsellorProfileRepository;
    private final SessionTypeRepository sessionTypeRepository;
    private final GamificationService gamificationService;
    
    public StudentController(StudentDashboardFacade dashboardFacade,
                            LearningModuleService moduleService,
                            QuizService quizService,
                            AssessmentService assessmentService,
                            MoodLogService moodLogService,
                            AppointmentService appointmentService,
                            AiCoachService aiCoachService,
                            CarePlanService carePlanService,
                            NudgeService nudgeService,
                            UserService userService,
                            AuditLogService auditLogService,
                            ApplicationEventPublisher eventPublisher,
                            CounsellorProfileRepository counsellorProfileRepository,
                            SessionTypeRepository sessionTypeRepository,
                            GamificationService gamificationService) {
        this.dashboardFacade = dashboardFacade;
        this.moduleService = moduleService;
        this.quizService = quizService;
        this.assessmentService = assessmentService;
        this.moodLogService = moodLogService;
        this.appointmentService = appointmentService;
        this.aiCoachService = aiCoachService;
        this.carePlanService = carePlanService;
        this.nudgeService = nudgeService;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.eventPublisher = eventPublisher;
        this.counsellorProfileRepository = counsellorProfileRepository;
        this.sessionTypeRepository = sessionTypeRepository;
        this.gamificationService = gamificationService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        StudentDashboardData data = dashboardFacade.getDashboardData(user.getId());
        model.addAttribute("dashboard", data);
        model.addAttribute("user", user);
        return "student/dashboard";
    }
    
    // ==================== LEARNING MODULES ====================
    
    @GetMapping("/modules")
    public String modules(Model model) {
        List<LearningModule> modules = moduleService.findPublished();
        model.addAttribute("modules", modules);
        return "student/modules";
    }
    
    @GetMapping("/modules/{id}")
    public String moduleDetail(@PathVariable Long id, 
                               @AuthenticationPrincipal CustomUserDetails user,
                               Model model) {
        LearningModule module = moduleService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        boolean hasQuiz = moduleService.hasQuiz(id);
        QuizAttempt lastAttempt = null;
        if (hasQuiz) {
            lastAttempt = quizService.getLatestAttempt(user.getId(), id).orElse(null);
        }
        
        model.addAttribute("module", module);
        model.addAttribute("hasQuiz", hasQuiz);
        model.addAttribute("lastAttempt", lastAttempt);
        return "student/module-detail";
    }
    
    // ==================== QUIZ ====================
    
    @GetMapping("/quiz/{moduleId}")
    public String quiz(@PathVariable Long moduleId,
                      @AuthenticationPrincipal CustomUserDetails user,
                      Model model) {
        LearningModule module = moduleService.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        List<QuizQuestion> questions = quizService.getQuestions(moduleId);
        
        model.addAttribute("module", module);
        model.addAttribute("questions", questions);
        return "student/quiz";
    }
    
    @PostMapping("/quiz/{moduleId}/submit")
    public String submitQuiz(@PathVariable Long moduleId,
                            @RequestParam Map<String, String> answers,
                            @AuthenticationPrincipal CustomUserDetails user,
                            RedirectAttributes redirectAttributes) {
        // Parse answers: q_123 -> 123, value -> A/B/C/D
        Map<Long, Character> parsedAnswers = new HashMap<>();
        answers.forEach((key, value) -> {
            if (key.startsWith("q_") && !value.isEmpty()) {
                Long questionId = Long.parseLong(key.substring(2));
                parsedAnswers.put(questionId, value.charAt(0));
            }
        });
        
        QuizAttempt attempt = quizService.submitQuiz(user.getId(), moduleId, parsedAnswers);
        
        redirectAttributes.addFlashAttribute("attempt", attempt);
        redirectAttributes.addFlashAttribute("success", "Quiz completed!");
        return "redirect:/student/quiz/" + moduleId + "/result";
    }
    
    @GetMapping("/quiz/{moduleId}/result")
    public String quizResult(@PathVariable Long moduleId,
                            @AuthenticationPrincipal CustomUserDetails user,
                            Model model) {
        LearningModule module = moduleService.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        QuizAttempt attempt = quizService.getLatestAttempt(user.getId(), moduleId)
            .orElseThrow(() -> new IllegalArgumentException("No quiz attempt found"));
        List<QuizQuestion> questions = quizService.getQuestions(moduleId);
        
        model.addAttribute("module", module);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        return "student/quiz-result";
    }
    
    // ==================== ASSESSMENT ====================
    
    @GetMapping("/assessment")
    public String assessment(Model model) {
        List<AssessmentDefinition> assessments = assessmentService.getActiveAssessments();
        model.addAttribute("assessments", assessments);
        return "student/assessment-list";
    }
    
    @GetMapping("/assessment/{code}")
    public String takeAssessment(@PathVariable String code,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 Model model) {
        AssessmentDefinition definition = assessmentService.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        List<AssessmentQuestion> questions = assessmentService.getQuestions(definition.getId());
        AssessmentResult lastResult = assessmentService.getLatestResult(user.getId(), definition.getId())
            .orElse(null);
        
        model.addAttribute("assessment", definition);
        model.addAttribute("questions", questions);
        model.addAttribute("lastResult", lastResult);
        return "student/assessment";
    }
    
    @PostMapping("/assessment/{code}/submit")
    public String submitAssessment(@PathVariable String code,
                                  @RequestParam Map<String, String> responses,
                                  @AuthenticationPrincipal CustomUserDetails user,
                                  RedirectAttributes redirectAttributes) {
        AssessmentDefinition definition = assessmentService.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        // Parse responses: q_123 -> 123, value -> 0-3
        Map<Long, Integer> parsedResponses = new HashMap<>();
        responses.forEach((key, value) -> {
            if (key.startsWith("q_") && !value.isEmpty()) {
                Long questionId = Long.parseLong(key.substring(2));
                parsedResponses.put(questionId, Integer.parseInt(value));
            }
        });
        
        try {
            AssessmentResult result = assessmentService.submitAssessment(
                user.getId(), definition.getId(), parsedResponses);
            
            // Log audit
            auditLogService.logAssessmentSubmitted(user.getId(), user.getEmail(), 
                result.getId(), result.getTotalScore(), result.getSeverity().name());
            
            // Publish event for Observer pattern
            eventPublisher.publishEvent(new AssessmentEventListener.AssessmentSubmittedEvent(this, result));
            
            redirectAttributes.addFlashAttribute("resultId", result.getId());
            return "redirect:/student/assessment/result/" + result.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/assessment/" + code;
        }
    }
    
    @GetMapping("/assessment/result/{id}")
    public String assessmentResultById(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails user, 
                                      Model model) {
        AssessmentResult result = assessmentService.findResultById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment result not found"));
        
        // Get feedback and recommendations from service
        String feedbackMessage = assessmentService.getFeedbackMessage(
            result.getDefinition().getCode(), result.getSeverity());
        List<String> recommendations = assessmentService.getRecommendations(
            result.getDefinition().getCode(), result.getSeverity());
        
        model.addAttribute("result", result);
        model.addAttribute("feedbackMessage", feedbackMessage);
        model.addAttribute("recommendations", recommendations);
        return "student/assessment-result";
    }
    
    @GetMapping("/assessment/result")
    public String assessmentResult(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        AssessmentResult result = assessmentService.getLatestResult(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("No assessment result found"));
        
        // Get feedback and recommendations from service
        String feedbackMessage = assessmentService.getFeedbackMessage(
            result.getDefinition().getCode(), result.getSeverity());
        List<String> recommendations = assessmentService.getRecommendations(
            result.getDefinition().getCode(), result.getSeverity());
        
        model.addAttribute("result", result);
        model.addAttribute("feedbackMessage", feedbackMessage);
        model.addAttribute("recommendations", recommendations);
        return "student/assessment-result";
    }
    
    @GetMapping("/assessment/history")
    public String assessmentHistory(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<AssessmentResult> results = assessmentService.getUserResults(user.getId());
        model.addAttribute("results", results);
        return "student/assessment-history";
    }
    
    // ==================== MOOD LOGGING ====================
    
    @GetMapping("/mood")
    public String mood(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<MoodLog> moodLogs = moodLogService.getRecentLogs(user.getId(), 7);
        model.addAttribute("moodLogs", moodLogs);
        return "student/mood";
    }
    
    @PostMapping("/mood/log")
    public String logMood(@RequestParam int moodValue,
                         @RequestParam(required = false) String note,
                         @AuthenticationPrincipal CustomUserDetails user,
                         RedirectAttributes redirectAttributes) {
        MoodLog moodLog = moodLogService.logMood(user.getId(), moodValue, note);
        
        auditLogService.logMoodLogged(user.getId(), user.getEmail(), moodLog.getId(), moodValue);
        
        redirectAttributes.addFlashAttribute("success", "Mood logged successfully!");
        return "redirect:/student/mood";
    }
    
    // ==================== AI COACH ====================
    
    @GetMapping("/ai-coach")
    public String aiCoach(@RequestParam(required = false) String sessionId,
                         @AuthenticationPrincipal CustomUserDetails user,
                         Model model) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = aiCoachService.generateSessionId();
        }
        
        List<ChatMessage> history = aiCoachService.getChatHistory(user.getId(), sessionId);
        List<String> suggestions = aiCoachService.getSuggestions(
            history.isEmpty() ? null : history.get(history.size() - 1).getMessage()
        );
        
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("history", history);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("aiEnabled", aiCoachService.isAiEnabled());
        return "student/ai-coach";
    }
    
    @PostMapping("/ai-coach/send")
    public String sendMessage(@RequestParam String sessionId,
                             @RequestParam String message,
                             @AuthenticationPrincipal CustomUserDetails user) {
        aiCoachService.processMessage(user.getId(), sessionId, message);
        return "redirect:/student/ai-coach?sessionId=" + sessionId;
    }
    
    @PostMapping("/ai-coach/send-ajax")
    @ResponseBody
    public Map<String, Object> sendMessageAjax(@RequestParam String sessionId,
                                               @RequestParam String message,
                                               @AuthenticationPrincipal CustomUserDetails user) {
        ChatMessage botResponse = aiCoachService.processMessage(user.getId(), sessionId, message);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("response", botResponse.getMessage());
        response.put("sessionId", sessionId);
        return response;
    }
    
    // ==================== CARE PLAN ====================
    
    @GetMapping("/care-plan")
    public String carePlan(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        CarePlan carePlan = carePlanService.getLatestCarePlan(user.getId())
            .orElseGet(() -> carePlanService.generateCarePlan(user.getId()));
        
        List<Map<String, String>> recommendations = carePlanService.parseRecommendations(
            carePlan.getRecommendationsJson()
        );
        
        model.addAttribute("carePlan", carePlan);
        model.addAttribute("recommendations", recommendations);
        return "student/care-plan";
    }
    
    // ==================== APPOINTMENTS ====================
    
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Appointment> appointments = appointmentService.getStudentAppointments(user.getId());
        List<CounsellorProfile> counsellorProfiles = counsellorProfileRepository.findAllActiveWithUser();
        List<SessionType> sessionTypes = sessionTypeRepository.findByActiveTrueOrderByDisplayOrderAsc();
        
        model.addAttribute("appointments", appointments);
        model.addAttribute("counsellorProfiles", counsellorProfiles);
        model.addAttribute("sessionTypes", sessionTypes);
        return "student/appointments";
    }
    
    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam Long counsellorId,
                                 @RequestParam String dateTime,
                                 @RequestParam(required = false) Long sessionTypeId,
                                 @RequestParam(required = false) String reason,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes redirectAttributes) {
        LocalDateTime startAt = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Get session type if provided
        SessionType sessionType = sessionTypeId != null ? 
            sessionTypeRepository.findById(sessionTypeId).orElse(null) : null;
        
        // Book appointment with session type included
        Appointment appointment = appointmentService.bookAppointment(
            user.getId(), counsellorId, startAt, reason, sessionType);
        
        auditLogService.logAppointmentCreated(user.getId(), user.getEmail(), appointment.getId());
        
        redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
        return "redirect:/student/appointments";
    }
    
    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                   @RequestParam(required = false) String reason,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   RedirectAttributes redirectAttributes) {
        appointmentService.cancel(id, reason);
        
        auditLogService.logAppointmentCancelled(user.getId(), user.getEmail(), id, reason);
        
        redirectAttributes.addFlashAttribute("success", "Appointment cancelled");
        return "redirect:/student/appointments";
    }
    
    // ==================== NUDGES ====================
    
    @PostMapping("/nudges/{id}/dismiss")
    public String dismissNudge(@PathVariable Long id,
                              @RequestHeader(value = "Referer", defaultValue = "/student/dashboard") String referer) {
        nudgeService.dismiss(id);
        return "redirect:" + referer;
    }
}

package com.dmhlh.controller;

import com.dmhlh.dto.AdminDashboardData;
import com.dmhlh.entity.*;
import com.dmhlh.facade.AdminDashboardFacade;
import com.dmhlh.repository.AssessmentDefinitionRepository;
import com.dmhlh.repository.AssessmentQuestionRepository;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final AdminDashboardFacade dashboardFacade;
    private final LearningModuleService moduleService;
    private final ForumService forumService;
    private final IntegrationSettingService integrationService;
    private final UserService userService;
    private final AssessmentService assessmentService;
    private final AssessmentDefinitionRepository assessmentDefinitionRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    
    public AdminController(AdminDashboardFacade dashboardFacade,
                          LearningModuleService moduleService,
                          ForumService forumService,
                          IntegrationSettingService integrationService,
                          UserService userService,
                          AssessmentService assessmentService,
                          AssessmentDefinitionRepository assessmentDefinitionRepository,
                          AssessmentQuestionRepository assessmentQuestionRepository) {
        this.dashboardFacade = dashboardFacade;
        this.moduleService = moduleService;
        this.forumService = forumService;
        this.integrationService = integrationService;
        this.userService = userService;
        this.assessmentService = assessmentService;
        this.assessmentDefinitionRepository = assessmentDefinitionRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        AdminDashboardData data = dashboardFacade.getDashboardData();
        
        model.addAttribute("user", user);
        model.addAttribute("kpis", data);
        model.addAttribute("currentPage", "dashboard");
        return "admin/dashboard";
    }
    
    // ==================== CONTENT MANAGEMENT ====================
    
    @GetMapping("/content")
    public String content(Model model) {
        List<LearningModule> modules = moduleService.findAll();
        model.addAttribute("modules", modules);
        return "admin/content";
    }
    
    @GetMapping("/content/new")
    public String newModuleForm(Model model) {
        model.addAttribute("types", LearningModule.ModuleType.values());
        model.addAttribute("statuses", LearningModule.Status.values());
        return "admin/content-form";
    }
    
    @PostMapping("/content/create")
    public String createModule(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam String type,
                              @RequestParam(required = false) String bodyContent,
                              @RequestParam(required = false) String videoUrl,
                              @RequestParam String status,
                              @RequestParam(required = false) Integer displayOrder,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        User creator = userService.findById(user.getId()).orElse(null);
        
        moduleService.create(
            title, description,
            LearningModule.ModuleType.valueOf(type),
            bodyContent, videoUrl,
            LearningModule.Status.valueOf(status),
            displayOrder, creator
        );
        
        redirectAttributes.addFlashAttribute("success", "Module created successfully");
        return "redirect:/admin/content";
    }
    
    @GetMapping("/content/{id}/edit")
    public String editModuleForm(@PathVariable Long id, Model model) {
        LearningModule module = moduleService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        model.addAttribute("module", module);
        model.addAttribute("types", LearningModule.ModuleType.values());
        model.addAttribute("statuses", LearningModule.Status.values());
        return "admin/content-edit";
    }
    
    @PostMapping("/content/{id}/update")
    public String updateModule(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam String type,
                              @RequestParam(required = false) String bodyContent,
                              @RequestParam(required = false) String videoUrl,
                              @RequestParam String status,
                              @RequestParam(required = false) Integer displayOrder,
                              RedirectAttributes redirectAttributes) {
        moduleService.update(
            id, title, description,
            LearningModule.ModuleType.valueOf(type),
            bodyContent, videoUrl,
            LearningModule.Status.valueOf(status),
            displayOrder
        );
        
        redirectAttributes.addFlashAttribute("success", "Module updated successfully");
        return "redirect:/admin/content";
    }
    
    @PostMapping("/content/{id}/delete")
    public String deleteModule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        moduleService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Module deleted");
        return "redirect:/admin/content";
    }
    
    // ==================== QUIZ MANAGEMENT ====================
    
    @GetMapping("/content/{id}/quiz")
    public String moduleQuiz(@PathVariable Long id, Model model) {
        LearningModule module = moduleService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        List<QuizQuestion> questions = moduleService.getQuizQuestions(id);
        
        model.addAttribute("module", module);
        model.addAttribute("questions", questions);
        return "admin/quiz-manage";
    }
    
    @PostMapping("/content/{id}/quiz/add")
    public String addQuestion(@PathVariable Long id,
                             @RequestParam String question,
                             @RequestParam String optionA,
                             @RequestParam String optionB,
                             @RequestParam String optionC,
                             @RequestParam String optionD,
                             @RequestParam char correctOption,
                             @RequestParam(required = false) String explanation,
                             @RequestParam(required = false) Integer displayOrder,
                             RedirectAttributes redirectAttributes) {
        moduleService.addQuestion(id, question, optionA, optionB, optionC, optionD,
            correctOption, explanation, displayOrder);
        
        redirectAttributes.addFlashAttribute("success", "Question added");
        return "redirect:/admin/content/" + id + "/quiz";
    }
    
    @PostMapping("/content/quiz/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId,
                                @RequestParam Long moduleId,
                                RedirectAttributes redirectAttributes) {
        moduleService.deleteQuestion(questionId);
        redirectAttributes.addFlashAttribute("success", "Question deleted");
        return "redirect:/admin/content/" + moduleId + "/quiz";
    }
    
    // ==================== FORUM SETTINGS ====================
    
    @GetMapping("/forum-settings")
    public String forumSettings(Model model) {
        ForumSettings settings = forumService.getSettings();
        model.addAttribute("settings", settings);
        return "admin/forum-settings";
    }
    
    @PostMapping("/forum-settings/update")
    public String updateForumSettings(@RequestParam(defaultValue = "false") boolean allowPosting,
                                     @RequestParam int maxPostLength,
                                     @RequestParam int maxTitleLength,
                                     @RequestParam(required = false) String bannedWords,
                                     @RequestParam(required = false) String moderationWords,
                                     @RequestParam(defaultValue = "false") boolean requireModeration,
                                     @RequestParam(defaultValue = "true") boolean allowAnonymous,
                                     RedirectAttributes redirectAttributes) {
        ForumSettings settings = forumService.getSettings();
        settings.setAllowPosting(allowPosting);
        settings.setMaxPostLength(maxPostLength);
        settings.setMaxTitleLength(maxTitleLength);
        settings.setBannedWords(bannedWords);
        settings.setModerationWords(moderationWords);
        settings.setRequireModeration(requireModeration);
        settings.setAllowAnonymous(allowAnonymous);
        
        forumService.updateSettings(settings);
        
        redirectAttributes.addFlashAttribute("success", "Forum settings updated");
        return "redirect:/admin/forum-settings";
    }
    
    // ==================== INTEGRATIONS ====================
    
    @GetMapping("/integrations")
    public String integrations(Model model) {
        List<IntegrationSetting> settings = integrationService.getAllSettings();
        model.addAttribute("settings", settings);
        return "admin/integrations";
    }
    
    @PostMapping("/integrations/save")
    public String saveIntegration(@RequestParam String key,
                                 @RequestParam(required = false) String value,
                                 @RequestParam(defaultValue = "false") boolean isSecret,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        integrationService.createOrUpdate(key, value, isSecret, description);
        
        redirectAttributes.addFlashAttribute("success", "Setting saved");
        return "redirect:/admin/integrations";
    }
    
    @PostMapping("/integrations/{id}/delete")
    public String deleteIntegration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        integrationService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Setting deleted");
        return "redirect:/admin/integrations";
    }
    
    // ==================== ASSESSMENT MANAGEMENT ====================
    
    @GetMapping("/assessments")
    public String assessments(Model model) {
        List<AssessmentDefinition> assessments = assessmentDefinitionRepository.findAll();
        model.addAttribute("assessments", assessments);
        return "admin/assessments";
    }
    
    @GetMapping("/assessments/new")
    public String newAssessmentForm(Model model) {
        return "admin/assessment-form";
    }
    
    @PostMapping("/assessments/create")
    public String createAssessment(@RequestParam String code,
                                  @RequestParam String name,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) String instructions,
                                  @RequestParam(required = false) Integer maxScore,
                                  @RequestParam(defaultValue = "true") boolean active,
                                  RedirectAttributes redirectAttributes) {
        AssessmentDefinition assessment = AssessmentDefinition.builder()
            .code(code.toUpperCase())
            .name(name)
            .description(description)
            .instructions(instructions)
            .maxScore(maxScore)
            .active(active)
            .build();
        
        assessmentDefinitionRepository.save(assessment);
        
        redirectAttributes.addFlashAttribute("success", "Assessment created successfully");
        return "redirect:/admin/assessments";
    }
    
    @GetMapping("/assessments/{id}/edit")
    public String editAssessmentForm(@PathVariable Long id, Model model) {
        AssessmentDefinition assessment = assessmentDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        model.addAttribute("assessment", assessment);
        return "admin/assessment-edit";
    }
    
    @PostMapping("/assessments/{id}/update")
    public String updateAssessment(@PathVariable Long id,
                                  @RequestParam String name,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) String instructions,
                                  @RequestParam(required = false) Integer maxScore,
                                  @RequestParam(defaultValue = "true") boolean active,
                                  RedirectAttributes redirectAttributes) {
        AssessmentDefinition assessment = assessmentDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        assessment.setName(name);
        assessment.setDescription(description);
        assessment.setInstructions(instructions);
        assessment.setMaxScore(maxScore);
        assessment.setActive(active);
        
        assessmentDefinitionRepository.save(assessment);
        
        redirectAttributes.addFlashAttribute("success", "Assessment updated successfully");
        return "redirect:/admin/assessments";
    }
    
    @PostMapping("/assessments/{id}/toggle")
    public String toggleAssessment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        AssessmentDefinition assessment = assessmentDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        assessment.setActive(!assessment.isActive());
        assessmentDefinitionRepository.save(assessment);
        
        redirectAttributes.addFlashAttribute("success", "Assessment " + (assessment.isActive() ? "activated" : "deactivated"));
        return "redirect:/admin/assessments";
    }
    
    // ==================== ASSESSMENT QUESTIONS ====================
    
    @GetMapping("/assessments/{id}/questions")
    public String assessmentQuestions(@PathVariable Long id, Model model) {
        AssessmentDefinition assessment = assessmentDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        List<AssessmentQuestion> questions = assessmentService.getQuestions(id);
        
        model.addAttribute("assessment", assessment);
        model.addAttribute("questions", questions);
        return "admin/assessment-questions";
    }
    
    @PostMapping("/assessments/{id}/questions/add")
    public String addAssessmentQuestion(@PathVariable Long id,
                                       @RequestParam String question,
                                       @RequestParam int displayOrder,
                                       @RequestParam(required = false) String option0,
                                       @RequestParam(required = false) String option1,
                                       @RequestParam(required = false) String option2,
                                       @RequestParam(required = false) String option3,
                                       RedirectAttributes redirectAttributes) {
        AssessmentDefinition definition = assessmentDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        AssessmentQuestion q = AssessmentQuestion.builder()
            .definition(definition)
            .question(question)
            .displayOrder(displayOrder)
            .option0Text(option0 != null ? option0 : "Not at all")
            .option1Text(option1 != null ? option1 : "Several days")
            .option2Text(option2 != null ? option2 : "More than half the days")
            .option3Text(option3 != null ? option3 : "Nearly every day")
            .build();
        
        assessmentQuestionRepository.save(q);
        
        redirectAttributes.addFlashAttribute("success", "Question added");
        return "redirect:/admin/assessments/" + id + "/questions";
    }
    
    @PostMapping("/assessments/questions/{questionId}/delete")
    public String deleteAssessmentQuestion(@PathVariable Long questionId,
                                          @RequestParam Long assessmentId,
                                          RedirectAttributes redirectAttributes) {
        assessmentQuestionRepository.deleteById(questionId);
        redirectAttributes.addFlashAttribute("success", "Question deleted");
        return "redirect:/admin/assessments/" + assessmentId + "/questions";
    }
}

package com.dmhlh.controller;

import com.dmhlh.entity.*;
import com.dmhlh.repository.*;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.AuditLogService;
import com.dmhlh.service.ForumService;
import com.dmhlh.service.GamificationService;
import com.dmhlh.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/forum")
public class ForumController {
    
    private final ForumService forumService;
    private final AuditLogService auditLogService;
    private final ForumTagRepository tagRepository;
    private final ForumDraftRepository draftRepository;
    private final ForumThreadLikeRepository likeRepository;
    private final ForumBookmarkRepository bookmarkRepository;
    private final ForumThreadRepository threadRepository;
    private final UserService userService;
    private final GamificationService gamificationService;
    
    public ForumController(ForumService forumService, 
                          AuditLogService auditLogService,
                          ForumTagRepository tagRepository,
                          ForumDraftRepository draftRepository,
                          ForumThreadLikeRepository likeRepository,
                          ForumBookmarkRepository bookmarkRepository,
                          ForumThreadRepository threadRepository,
                          UserService userService,
                          GamificationService gamificationService) {
        this.forumService = forumService;
        this.auditLogService = auditLogService;
        this.tagRepository = tagRepository;
        this.draftRepository = draftRepository;
        this.likeRepository = likeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.threadRepository = threadRepository;
        this.userService = userService;
        this.gamificationService = gamificationService;
    }
    
    @GetMapping
    public String forum(@AuthenticationPrincipal CustomUserDetails user, 
                       @RequestParam(defaultValue = "hot") String filter,
                       @RequestParam(required = false) String tag,
                       Model model) {
        // Get threads based on filter or tag
        List<ForumThread> threads;
        if (tag != null && !tag.isEmpty()) {
            threads = forumService.getThreadsByTag(tag);
        } else {
            threads = forumService.getActiveThreads(filter, user.getId());
        }
        
        ForumSettings settings = forumService.getSettings();
        List<ForumTag> allTags = tagRepository.findAllByOrderByNameAsc();
        
        // Get trending tags with actual counts
        List<ForumTag> trendingTags = getTrendingTagsWithCounts();
        
        List<ForumDraft> drafts = draftRepository.findByUserId(user.getId());
        
        // Get user's likes and bookmarks
        Set<Long> likedThreadIds = likeRepository.findThreadIdsByUserId(user.getId());
        Set<Long> bookmarkedThreadIds = bookmarkRepository.findThreadIdsByUserId(user.getId());
        
        // Update thread states
        threads.forEach(thread -> {
            thread.setLikedByCurrentUser(likedThreadIds.contains(thread.getId()));
            thread.setBookmarkedByCurrentUser(bookmarkedThreadIds.contains(thread.getId()));
        });
        
        // Generate anonymous alias for posting
        String anonymousAlias = forumService.generateAnonymousAlias();
        
        model.addAttribute("threads", threads);
        model.addAttribute("settings", settings);
        model.addAttribute("allTags", allTags);
        model.addAttribute("trendingTags", trendingTags);
        model.addAttribute("drafts", drafts);
        model.addAttribute("anonymousAlias", anonymousAlias);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentTag", tag);
        return "forum/index";
    }
    
    private List<ForumTag> getTrendingTagsWithCounts() {
        List<Object[]> results = tagRepository.findTrendingTagsWithCount();
        List<ForumTag> trendingTags = new ArrayList<>();
        
        for (Object[] row : results) {
            ForumTag tag = (ForumTag) row[0];
            Long count = (Long) row[1];
            tag.setPostCount(count.intValue());
            trendingTags.add(tag);
            if (trendingTags.size() >= 8) break;
        }
        
        return trendingTags;
    }
    
    @GetMapping("/thread/{id}")
    public String thread(@PathVariable Long id, Model model) {
        ForumThread thread = forumService.findThreadById(id)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        List<ForumPost> posts = forumService.getThreadPosts(id);
        
        // Increment view count
        forumService.incrementViewCount(id);
        
        model.addAttribute("thread", thread);
        model.addAttribute("posts", posts);
        model.addAttribute("reportReasons", ForumReport.ReportReason.values());
        return "forum/thread";
    }
    
    @GetMapping("/new")
    public String newThreadForm(Model model) {
        ForumSettings settings = forumService.getSettings();
        if (!settings.isAllowPosting()) {
            model.addAttribute("error", "Posting is currently disabled");
            return "redirect:/forum";
        }
        model.addAttribute("settings", settings);
        return "forum/new-thread";
    }
    
    @PostMapping("/create")
    public String createThread(@RequestParam String title,
                              @RequestParam String content,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        try {
            ForumThread thread = forumService.createThread(user.getId(), title, content);
            
            auditLogService.logPostCreated(user.getId(), user.getEmail(), thread.getId(), "THREAD");
            
            // Check if thread is pending moderation
            if (forumService.isPendingModeration(thread)) {
                redirectAttributes.addFlashAttribute("info", 
                    "Your post has been submitted for review. It will be visible once approved by a moderator. " +
                    "If you're going through a difficult time, please reach out to a counselor.");
                return "redirect:/forum";
            }
            
            return "redirect:/forum/thread/" + thread.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forum/new";
        }
    }
    
    @PostMapping("/thread/{id}/reply")
    public String reply(@PathVariable Long id,
                       @RequestParam String content,
                       @RequestParam(required = false) Long parentPostId,
                       @AuthenticationPrincipal CustomUserDetails user,
                       RedirectAttributes redirectAttributes) {
        try {
            ForumPost post = forumService.createPost(id, user.getId(), content, parentPostId);
            
            auditLogService.logPostCreated(user.getId(), user.getEmail(), post.getId(), "REPLY");
            
            // Check if reply is pending moderation
            if (post.getStatus() == ForumPost.Status.PENDING_MODERATION) {
                redirectAttributes.addFlashAttribute("info", 
                    "Your reply has been submitted for review. It will be visible once approved by a moderator. " +
                    "If you're going through a difficult time, please reach out to a counselor.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Reply posted successfully!");
            }
            
            return "redirect:/forum/thread/" + id;
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forum/thread/" + id;
        }
    }
    
    @PostMapping("/report/post/{id}")
    public String reportPost(@PathVariable Long id,
                            @RequestParam String reason,
                            @RequestParam(required = false) String details,
                            @AuthenticationPrincipal CustomUserDetails user,
                            RedirectAttributes redirectAttributes) {
        ForumReport.ReportReason reportReason = ForumReport.ReportReason.valueOf(reason);
        ForumReport report = forumService.reportPost(id, user.getId(), reportReason, details);
        
        auditLogService.logPostReported(user.getId(), user.getEmail(), report.getId(), reason);
        
        redirectAttributes.addFlashAttribute("success", "Report submitted. Thank you for helping keep our community safe.");
        return "redirect:/forum";
    }
    
    @PostMapping("/report/thread/{id}")
    public String reportThread(@PathVariable Long id,
                              @RequestParam String reason,
                              @RequestParam(required = false) String details,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        ForumReport.ReportReason reportReason = ForumReport.ReportReason.valueOf(reason);
        ForumReport report = forumService.reportThread(id, user.getId(), reportReason, details);
        
        auditLogService.logPostReported(user.getId(), user.getEmail(), report.getId(), reason);
        
        redirectAttributes.addFlashAttribute("success", "Report submitted. Thank you for helping keep our community safe.");
        return "redirect:/forum";
    }
    
    // ==================== LIKES ====================
    
    @PostMapping("/thread/{id}/like")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id,
                                                         @AuthenticationPrincipal CustomUserDetails user) {
        ForumThread thread = threadRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        User currentUser = userService.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        boolean liked;
        Optional<ForumThreadLike> existingLike = likeRepository.findByThreadAndUser(thread, currentUser);
        
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            thread.setLikeCount(Math.max(0, thread.getLikeCount() - 1));
            liked = false;
        } else {
            ForumThreadLike like = ForumThreadLike.builder()
                .thread(thread)
                .user(currentUser)
                .build();
            likeRepository.save(like);
            thread.setLikeCount(thread.getLikeCount() + 1);
            liked = true;
        }
        
        threadRepository.save(thread);
        
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", thread.getLikeCount());
        return ResponseEntity.ok(response);
    }
    
    // ==================== BOOKMARKS ====================
    
    @PostMapping("/thread/{id}/bookmark")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserDetails user) {
        ForumThread thread = threadRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        User currentUser = userService.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        boolean bookmarked;
        Optional<ForumBookmark> existingBookmark = bookmarkRepository.findByThreadAndUser(thread, currentUser);
        
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            bookmarked = false;
        } else {
            ForumBookmark bookmark = ForumBookmark.builder()
                .thread(thread)
                .user(currentUser)
                .build();
            bookmarkRepository.save(bookmark);
            bookmarked = true;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        return ResponseEntity.ok(response);
    }
    
    // ==================== DRAFTS ====================
    
    @PostMapping("/draft/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal CustomUserDetails user) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        @SuppressWarnings("unchecked")
        List<String> tagIds = (List<String>) body.get("tagIds");
        
        User currentUser = userService.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ForumDraft draft = ForumDraft.builder()
            .user(currentUser)
            .title(title)
            .content(content)
            .tagsJson(tagIds != null ? String.join(",", tagIds) : null)
            .build();
        
        draftRepository.save(draft);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("draftId", draft.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/draft/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable Long id,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        ForumDraft draft = draftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        
        if (!draft.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", draft.getId());
        response.put("title", draft.getTitle());
        response.put("content", draft.getContent());
        response.put("tagsJson", draft.getTagsJson());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/draft/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteDraft(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails user) {
        ForumDraft draft = draftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        
        if (!draft.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        draftRepository.delete(draft);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    // ==================== THREAD CONTENT FOR MODAL ====================
    
    @GetMapping("/thread/{id}/content")
    public String threadContent(@PathVariable Long id, Model model) {
        ForumThread thread = forumService.findThreadById(id)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        List<ForumPost> posts = forumService.getThreadPosts(id);
        
        forumService.incrementViewCount(id);
        
        model.addAttribute("thread", thread);
        model.addAttribute("posts", posts);
        return "forum/thread-content :: threadContent";
    }
}

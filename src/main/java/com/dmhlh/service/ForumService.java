package com.dmhlh.service;

import com.dmhlh.entity.*;
import com.dmhlh.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ForumService {
    
    private final ForumThreadRepository threadRepository;
    private final ForumPostRepository postRepository;
    private final ForumReportRepository reportRepository;
    private final ForumSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    
    // Adjectives and nouns for anonymous alias generation
    private static final String[] ADJECTIVES = {
        "Calm", "Brave", "Kind", "Gentle", "Wise", "Hopeful", "Peaceful", "Strong",
        "Warm", "Bright", "Caring", "Mindful", "Serene", "Resilient", "Grateful"
    };
    
    private static final String[] NOUNS = {
        "Penguin", "Butterfly", "Phoenix", "Dolphin", "Owl", "Deer", "Swan",
        "Robin", "Lotus", "Star", "River", "Mountain", "Cloud", "Sunbeam", "Meadow"
    };
    
    public ForumService(ForumThreadRepository threadRepository,
                       ForumPostRepository postRepository,
                       ForumReportRepository reportRepository,
                       ForumSettingsRepository settingsRepository,
                       UserRepository userRepository,
                       GamificationService gamificationService) {
        this.threadRepository = threadRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }
    
    public String generateAnonymousAlias() {
        Random random = new Random();
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int num = random.nextInt(100);
        return adj + noun + num;
    }
    
    public List<ForumThread> getActiveThreads() {
        return threadRepository.findActiveThreads();
    }
    
    public List<ForumThread> getActiveThreads(String filter, Long userId) {
        List<ForumThread> threads;
        switch (filter) {
            case "new":
                threads = threadRepository.findActiveThreadsByNew();
                break;
            case "top":
                threads = threadRepository.findActiveThreadsByPopularity();
                break;
            case "bookmarked":
                threads = userId != null ? threadRepository.findBookmarkedByUser(userId) : threadRepository.findActiveThreads();
                break;
            default: // "hot" or any other
                threads = threadRepository.findActiveThreads();
        }
        
        // Update actual reply counts from database
        threads.forEach(thread -> {
            int actualCount = threadRepository.countActivePostsByThreadId(thread.getId());
            thread.setReplyCount(actualCount);
        });
        
        return threads;
    }
    
    public List<ForumThread> getThreadsByTag(String tagName) {
        return threadRepository.findByTagName(tagName);
    }
    
    public Optional<ForumThread> findThreadById(Long id) {
        return threadRepository.findById(id);
    }
    
    public List<ForumPost> getThreadPosts(Long threadId) {
        return postRepository.findByThreadIdAndStatusOrderByCreatedAtAsc(threadId, ForumPost.Status.ACTIVE);
    }
    
    public ForumSettings getSettings() {
        return settingsRepository.getSettings();
    }
    
    @Transactional
    public ForumThread createThread(Long authorId, String title, String content) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ForumSettings settings = getSettings();
        if (!settings.isAllowPosting()) {
            throw new IllegalStateException("Forum posting is currently disabled");
        }
        
        // Validate content (checks banned words - hard block)
        validateContent(title, content, settings);
        
        // Check if content requires moderation (potentially harmful - soft block)
        boolean needsModeration = requiresModeration(title, content, settings);
        
        String alias = generateAnonymousAlias();
        
        ForumThread thread = ForumThread.builder()
            .author(author)
            .anonymousAlias(alias)
            .title(title)
            .content(content)
            .status(needsModeration ? ForumThread.Status.PENDING_MODERATION : ForumThread.Status.ACTIVE)
            .build();
        
        ForumThread saved = threadRepository.save(thread);
        
        // Only award points if post is active (not pending moderation)
        if (!needsModeration) {
            gamificationService.awardPointsForForumPost(author, saved.getId());
        }
        
        return saved;
    }
    
    /**
     * Check if thread is pending moderation
     */
    public boolean isPendingModeration(ForumThread thread) {
        return thread.getStatus() == ForumThread.Status.PENDING_MODERATION;
    }
    
    @Transactional
    public ForumPost createPost(Long threadId, Long authorId, String content, Long parentPostId) {
        ForumThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        
        if (thread.isLocked()) {
            throw new IllegalStateException("Thread is locked");
        }
        
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ForumSettings settings = getSettings();
        if (!settings.isAllowPosting()) {
            throw new IllegalStateException("Forum posting is currently disabled");
        }
        
        // Validate content (checks banned words - hard block)
        validateContent(null, content, settings);
        
        // Check if content requires moderation (potentially harmful - soft block)
        boolean needsModeration = requiresModeration(null, content, settings);
        
        // Use same alias if author posted before in this thread, otherwise generate new
        String alias = findExistingAlias(threadId, authorId)
            .orElseGet(this::generateAnonymousAlias);
        
        ForumPost.ForumPostBuilder builder = ForumPost.builder()
            .thread(thread)
            .author(author)
            .anonymousAlias(alias)
            .content(content)
            .status(needsModeration ? ForumPost.Status.PENDING_MODERATION : ForumPost.Status.ACTIVE);
        
        if (parentPostId != null) {
            ForumPost parent = postRepository.findById(parentPostId).orElse(null);
            builder.parentPost(parent);
        }
        
        ForumPost post = builder.build();
        ForumPost savedPost = postRepository.save(post);
        
        // Update thread timestamp and reply count (only if active)
        if (!needsModeration) {
            thread.setUpdatedAt(LocalDateTime.now());
            thread.setReplyCount(thread.getReplyCount() + 1);
            threadRepository.save(thread);
        }
        
        return savedPost;
    }
    
    private Optional<String> findExistingAlias(Long threadId, Long authorId) {
        // Check thread author
        ForumThread thread = threadRepository.findById(threadId).orElse(null);
        if (thread != null && thread.getAuthor().getId().equals(authorId)) {
            return Optional.of(thread.getAnonymousAlias());
        }
        
        // Check existing posts
        return postRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
            .filter(p -> p.getAuthor().getId().equals(authorId))
            .map(ForumPost::getAnonymousAlias)
            .findFirst();
    }
    
    @Transactional
    public ForumReport reportPost(Long postId, Long reporterId, ForumReport.ReportReason reason, String details) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ForumReport report = ForumReport.builder()
            .post(post)
            .thread(post.getThread())
            .reporter(reporter)
            .reason(reason)
            .details(details)
            .status(ForumReport.Status.PENDING)
            .build();
        
        return reportRepository.save(report);
    }
    
    @Transactional
    public ForumReport reportThread(Long threadId, Long reporterId, ForumReport.ReportReason reason, String details) {
        ForumThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ForumReport report = ForumReport.builder()
            .thread(thread)
            .reporter(reporter)
            .reason(reason)
            .details(details)
            .status(ForumReport.Status.PENDING)
            .build();
        
        return reportRepository.save(report);
    }
    
    public List<ForumReport> getPendingReports() {
        return reportRepository.findByStatusInOrderByCreatedAtDesc(
            List.of(ForumReport.Status.PENDING, ForumReport.Status.UNDER_REVIEW)
        );
    }
    
    public Optional<ForumReport> findReportById(Long id) {
        return reportRepository.findById(id);
    }
    
    @Transactional
    public void resolveReport(Long reportId, Long moderatorId, String action, String note) {
        ForumReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        User moderator = userRepository.findById(moderatorId)
            .orElseThrow(() -> new IllegalArgumentException("Moderator not found"));
        
        report.setStatus(ForumReport.Status.RESOLVED);
        report.setResolvedBy(moderator);
        report.setResolutionNote(note);
        report.setResolvedAt(LocalDateTime.now());
        
        // Take action on the reported content
        if ("REMOVE".equals(action)) {
            if (report.getPost() != null) {
                report.getPost().setStatus(ForumPost.Status.REMOVED);
                postRepository.save(report.getPost());
            }
            if (report.getThread() != null && report.getPost() == null) {
                report.getThread().setStatus(ForumThread.Status.REMOVED);
                threadRepository.save(report.getThread());
            }
        } else if ("HIDE".equals(action)) {
            if (report.getPost() != null) {
                report.getPost().setStatus(ForumPost.Status.HIDDEN);
                postRepository.save(report.getPost());
            }
            if (report.getThread() != null && report.getPost() == null) {
                report.getThread().setStatus(ForumThread.Status.HIDDEN);
                threadRepository.save(report.getThread());
            }
        }
        
        reportRepository.save(report);
    }
    
    @Transactional
    public void dismissReport(Long reportId, Long moderatorId, String note) {
        ForumReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        User moderator = userRepository.findById(moderatorId)
            .orElseThrow(() -> new IllegalArgumentException("Moderator not found"));
        
        report.setStatus(ForumReport.Status.DISMISSED);
        report.setResolvedBy(moderator);
        report.setResolutionNote(note);
        report.setResolvedAt(LocalDateTime.now());
        
        reportRepository.save(report);
    }
    
    @Transactional
    public void updateSettings(ForumSettings settings) {
        settingsRepository.save(settings);
    }
    
    @Transactional
    public void incrementViewCount(Long threadId) {
        threadRepository.findById(threadId).ifPresent(thread -> {
            thread.setViewCount(thread.getViewCount() + 1);
            threadRepository.save(thread);
        });
    }
    
    private void validateContent(String title, String content, ForumSettings settings) {
        if (title != null && title.length() > settings.getMaxTitleLength()) {
            throw new IllegalArgumentException("Title exceeds maximum length");
        }
        
        if (content != null && content.length() > settings.getMaxPostLength()) {
            throw new IllegalArgumentException("Content exceeds maximum length");
        }
        
        // Check banned words - Hard block
        String bannedWords = settings.getBannedWords();
        if (bannedWords != null && !bannedWords.isEmpty()) {
            String[] banned = bannedWords.toLowerCase().split(",");
            String checkText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
            
            for (String word : banned) {
                if (checkText.contains(word.trim())) {
                    throw new IllegalArgumentException("Content contains prohibited words");
                }
            }
        }
    }
    
    /**
     * Checks if content contains potentially harmful words that require moderation.
     * Returns true if content should be flagged for moderation review.
     */
    private boolean requiresModeration(String title, String content, ForumSettings settings) {
        if (!settings.isRequireModeration()) {
            return false;
        }
        
        String moderationWords = settings.getModerationWords();
        // Default harmful/sensitive words if none configured
        if (moderationWords == null || moderationWords.isEmpty()) {
            moderationWords = "suicide,kill myself,self harm,cutting,overdose,end it all,want to die,hurting myself,harm myself,no reason to live";
        }
        
        String[] flagWords = moderationWords.toLowerCase().split(",");
        String checkText = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
        
        for (String word : flagWords) {
            if (checkText.contains(word.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public long countOpenReports() {
        return reportRepository.countOpenReports();
    }
    
    // ============= Moderation Methods =============
    
    /**
     * Get all threads pending moderation
     */
    public List<ForumThread> getPendingThreads() {
        return threadRepository.findByStatus(ForumThread.Status.PENDING_MODERATION);
    }
    
    /**
     * Get all posts pending moderation
     */
    public List<ForumPost> getPendingPosts() {
        return postRepository.findByStatus(ForumPost.Status.PENDING_MODERATION);
    }
    
    /**
     * Count pending moderation items (threads + posts)
     */
    public long countPendingModeration() {
        return threadRepository.countByStatus(ForumThread.Status.PENDING_MODERATION) +
               postRepository.countByStatus(ForumPost.Status.PENDING_MODERATION);
    }
    
    /**
     * Approve a pending thread - makes it visible to all users
     */
    @Transactional
    public void approveThread(Long threadId) {
        ForumThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        
        if (thread.getStatus() == ForumThread.Status.PENDING_MODERATION) {
            thread.setStatus(ForumThread.Status.ACTIVE);
            threadRepository.save(thread);
            
            // Award points now that it's approved
            gamificationService.awardPointsForForumPost(thread.getAuthor(), thread.getId());
        }
    }
    
    /**
     * Reject a pending thread - removes it
     */
    @Transactional
    public void rejectThread(Long threadId) {
        ForumThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        
        thread.setStatus(ForumThread.Status.REMOVED);
        threadRepository.save(thread);
    }
    
    /**
     * Approve a pending post - makes it visible to all users
     */
    @Transactional
    public void approvePost(Long postId) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (post.getStatus() == ForumPost.Status.PENDING_MODERATION) {
            post.setStatus(ForumPost.Status.ACTIVE);
            postRepository.save(post);
            
            // Update thread reply count
            ForumThread thread = post.getThread();
            thread.setReplyCount(thread.getReplyCount() + 1);
            thread.setUpdatedAt(LocalDateTime.now());
            threadRepository.save(thread);
        }
    }
    
    /**
     * Reject a pending post - removes it
     */
    @Transactional
    public void rejectPost(Long postId) {
        ForumPost post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setStatus(ForumPost.Status.REMOVED);
        postRepository.save(post);
    }
}

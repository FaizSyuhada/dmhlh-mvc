package com.dmhlh.service;

import com.dmhlh.entity.ChatMessage;
import com.dmhlh.entity.User;
import com.dmhlh.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiCoachService {
    
    private static final Logger log = LoggerFactory.getLogger(AiCoachService.class);
    
    private final ChatMessageRepository chatRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;
    
    @Value("${ai.gemini.enabled:false}")
    private boolean geminiEnabled;
    
    // System prompt for Mochi the wellness companion
    private static final String SYSTEM_PROMPT = """
        You are Mochi, a friendly and empathetic AI wellness companion for a campus mental health support platform called Solace.
        
        Your personality:
        - Warm, supportive, and non-judgmental
        - You use a casual but professional tone
        - You occasionally use emojis to be friendly (but not excessively)
        - You validate feelings and provide emotional support
        
        Your guidelines:
        - Keep responses concise (2-4 sentences unless more detail is needed)
        - Always validate the user's feelings first
        - Suggest practical coping strategies when appropriate
        - Recommend professional support (counselling) for serious concerns
        - For crisis situations (self-harm, suicide), immediately encourage seeking professional help
        - Never diagnose or provide medical advice
        - Remind users you're an AI and encourage them to speak with counsellors for deeper support
        
        Available resources you can recommend:
        - Self-assessments (PHQ-9 for depression, GAD-7 for anxiety)
        - Counselling appointments with our team
        - Learning modules on topics like anxiety and stress management
        - Mood journaling for tracking emotional patterns
        
        Remember: You're here to listen, support, and guide - not to replace professional mental health care.
        """;
    
    // Keyword patterns and responses (fallback)
    private static final Map<Pattern, List<String>> KEYWORD_RESPONSES = new LinkedHashMap<>();
    
    static {
        // Crisis/Emergency keywords - highest priority
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(suicid|kill myself|end my life|don't want to live|hurt myself|self.?harm).*"),
            List.of(
                "I'm really concerned about what you're sharing 💙 Your safety matters most right now. Please reach out to a crisis helpline or go to your nearest emergency room. You can also book an urgent appointment with our counselling team. Would you like me to help you find resources?",
                "What you're feeling sounds really serious, and I want you to know that help is available right now. Please consider calling a crisis line or visiting the emergency room. Our counsellors are also here for you. Can I help you book an appointment?"
            )
        );
        
        // Anxiety/Stress keywords
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(anxious|anxiety|panic|worried|worrying|nervous|stress|stressed|overwhelmed).*"),
            List.of(
                "It sounds like you're dealing with a lot of stress or anxiety right now. That can be really tough 💛 Have you tried any breathing exercises? Taking slow, deep breaths can help calm your nervous system. Would you like to try a quick breathing exercise together?",
                "Anxiety and stress are very common experiences, especially in a campus environment. Remember that these feelings are temporary. Have you checked out our 'Managing Academic Stress' module? It has some helpful strategies.",
                "I hear you - feeling anxious can be exhausting 😔 Some things that might help: taking a short walk, talking to a friend, or practicing mindfulness. Would you like to explore some coping strategies together?"
            )
        );
        
        // Depression/Sadness keywords
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(sad|depressed|depression|hopeless|empty|lonely|alone|down|unhappy|miserable).*"),
            List.of(
                "I'm sorry you're feeling this way 💙 It takes courage to share these feelings. Remember, you don't have to face this alone. Have you considered talking to one of our counsellors? They're here to support you.",
                "Feeling sad or down is a natural human experience, but when it persists, it's important to reach out for support. Would it help to take our PHQ-9 self-assessment to better understand how you're feeling?",
                "Thank you for sharing that with me. Your feelings are valid 💛 Sometimes talking to someone can really help. Our counselling team is available if you'd like to book an appointment."
            )
        );
        
        // Sleep issues
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(can't sleep|insomnia|sleep problem|tired|exhausted|fatigue|no energy).*"),
            List.of(
                "Sleep issues can really affect how we feel and function 😴 Some tips that might help: keeping a regular sleep schedule, avoiding screens before bed, and limiting caffeine. Have you tried any of these?",
                "Feeling tired and having trouble sleeping is common when we're stressed. It might help to create a calming bedtime routine. Would you like to explore some sleep hygiene tips?"
            )
        );
        
        // Exam/Academic stress
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(exam|test|assignment|deadline|grade|fail|study|academic).*"),
            List.of(
                "Academic pressure can feel overwhelming sometimes 📚 Remember to take breaks and be kind to yourself. Have you tried breaking your study sessions into smaller chunks? The Pomodoro technique can be really helpful.",
                "Exam stress is something many students experience. Our 'Managing Academic Stress' module has some great strategies. Would you like me to share some quick study tips?",
                "It's normal to feel stressed about exams and grades 💪 Remember that your worth isn't defined by your academic performance. Taking care of your mental health is just as important as studying."
            )
        );
        
        // Relationship issues
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(relationship|friend|family|conflict|argument|fight|breakup|dating).*"),
            List.of(
                "Relationship challenges can be really emotionally draining 💔 It's okay to take time to process your feelings. Would it help to talk to a counsellor about what you're going through?",
                "Navigating relationships can be complicated. Remember to communicate openly and set healthy boundaries. Our counsellors can help you work through relationship concerns if you'd like."
            )
        );
        
        // General wellbeing check
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i).*(how are you|help me|need help|support|talk|feeling|feel).*"),
            List.of(
                "I'm here to listen and support you 💙 What's on your mind today?",
                "Thank you for reaching out. I'm here to help however I can. What would you like to talk about?",
                "I'm glad you're here 🌟 How can I support you today?"
            )
        );
        
        // Greeting
        KEYWORD_RESPONSES.put(
            Pattern.compile("(?i)^(hi|hello|hey|good morning|good afternoon|good evening).*"),
            List.of(
                "Hello! 👋 Welcome to your mental health support space. How are you feeling today?",
                "Hi there! I'm here to support you. What's on your mind?",
                "Hello! 🌟 I'm glad you're here. How can I help you today?"
            )
        );
    }
    
    // Default responses when no keywords match
    private static final List<String> DEFAULT_RESPONSES = List.of(
        "Thank you for sharing 💙 Can you tell me more about what you're experiencing?",
        "I hear you. What else would you like to talk about?",
        "I'm listening. Feel free to share more about how you're feeling.",
        "That's interesting. How does that make you feel?",
        "I appreciate you opening up 🌟 Is there anything specific you'd like support with today?"
    );
    
    private final Random random = new Random();
    
    public AiCoachService(ChatMessageRepository chatRepository, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.chatRepository = chatRepository;
        this.webClient = webClientBuilder
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();
        this.objectMapper = objectMapper;
    }
    
    public List<ChatMessage> getChatHistory(Long userId, String sessionId) {
        return chatRepository.findTop50ByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }
    
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    @Transactional
    public ChatMessage processMessage(Long userId, String sessionId, String userMessage) {
        User user = new User();
        user.setId(userId);
        
        // Save user message
        ChatMessage userMsg = ChatMessage.builder()
            .user(user)
            .sessionId(sessionId)
            .sender(ChatMessage.Sender.USER)
            .message(userMessage)
            .build();
        chatRepository.save(userMsg);
        
        // Generate bot response (try AI first, fallback to keywords)
        String botResponse = generateResponse(userId, sessionId, userMessage);
        
        // Save bot message
        ChatMessage botMsg = ChatMessage.builder()
            .user(user)
            .sessionId(sessionId)
            .sender(ChatMessage.Sender.BOT)
            .message(botResponse)
            .build();
        
        return chatRepository.save(botMsg);
    }
    
    private String generateResponse(Long userId, String sessionId, String userMessage) {
        // Check for crisis keywords first - always handle these with predefined responses
        Pattern crisisPattern = Pattern.compile("(?i).*(suicid|kill myself|end my life|don't want to live|hurt myself|self.?harm).*");
        if (crisisPattern.matcher(userMessage).matches()) {
            List<String> responses = KEYWORD_RESPONSES.get(crisisPattern);
            return responses.get(random.nextInt(responses.size()));
        }
        
        // Try Gemini AI if enabled and configured
        if (geminiEnabled && geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                String aiResponse = callGeminiApi(userId, sessionId, userMessage);
                if (aiResponse != null && !aiResponse.isEmpty()) {
                    return aiResponse;
                }
            } catch (Exception e) {
                log.warn("Gemini API call failed, falling back to keyword responses: {}", e.getMessage());
            }
        }
        
        // Fallback to keyword-based responses
        return generateKeywordResponse(userMessage);
    }
    
    private String callGeminiApi(Long userId, String sessionId, String userMessage) {
        try {
            // Get recent chat history for context
            List<ChatMessage> history = getChatHistory(userId, sessionId);
            
            // Build conversation history
            StringBuilder conversationContext = new StringBuilder();
            conversationContext.append(SYSTEM_PROMPT).append("\n\n");
            conversationContext.append("Conversation history:\n");
            
            // Include last 10 messages for context
            int startIdx = Math.max(0, history.size() - 10);
            for (int i = startIdx; i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                String role = msg.getSender() == ChatMessage.Sender.USER ? "User" : "Mochi";
                conversationContext.append(role).append(": ").append(msg.getMessage()).append("\n");
            }
            conversationContext.append("User: ").append(userMessage).append("\n");
            conversationContext.append("Mochi: ");
            
            // Build request body for Gemini API
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", conversationContext.toString())
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 256,
                    "topP", 0.9
                ),
                "safetySettings", List.of(
                    Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                    Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                    Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                    Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
                )
            );
            
            // Call Gemini API
            String response = webClient.post()
                .uri("/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            // Parse response
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).path("content").path("parts");
                    if (content.isArray() && content.size() > 0) {
                        String text = content.get(0).path("text").asText();
                        // Clean up response
                        text = text.trim();
                        if (!text.isEmpty()) {
                            log.debug("Gemini response: {}", text);
                            return text;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
        }
        return null;
    }
    
    private String generateKeywordResponse(String userMessage) {
        // Check each keyword pattern in order (priority matters)
        for (Map.Entry<Pattern, List<String>> entry : KEYWORD_RESPONSES.entrySet()) {
            if (entry.getKey().matcher(userMessage).matches()) {
                List<String> responses = entry.getValue();
                return responses.get(random.nextInt(responses.size()));
            }
        }
        
        // Default response if no keywords match
        return DEFAULT_RESPONSES.get(random.nextInt(DEFAULT_RESPONSES.size()));
    }
    
    // Suggestions based on user context
    public List<String> getSuggestions(String lastMessage) {
        List<String> suggestions = new ArrayList<>();
        
        if (lastMessage == null || lastMessage.isEmpty()) {
            suggestions.add("I'm feeling stressed");
            suggestions.add("I need someone to talk to");
            suggestions.add("Help me understand my feelings");
            return suggestions;
        }
        
        String lower = lastMessage.toLowerCase();
        
        if (lower.contains("stress") || lower.contains("anxious")) {
            suggestions.add("Tell me about breathing exercises");
            suggestions.add("I want to book a counselling session");
            suggestions.add("Show me the stress management module");
        } else if (lower.contains("sad") || lower.contains("depressed")) {
            suggestions.add("I'd like to take an assessment");
            suggestions.add("Help me find a counsellor");
            suggestions.add("What resources are available?");
        } else {
            suggestions.add("I want to log my mood");
            suggestions.add("Show me learning modules");
            suggestions.add("Book a counselling appointment");
        }
        
        return suggestions;
    }
    
    /**
     * Check if AI is enabled and configured
     */
    public boolean isAiEnabled() {
        return geminiEnabled && geminiApiKey != null && !geminiApiKey.isEmpty();
    }
}

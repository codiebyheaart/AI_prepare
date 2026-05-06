package com.finsight.ai.chat;

import com.finsight.ai.rag.RagService;
import com.finsight.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat service managing conversation sessions with memory.
 * Each session keeps last 10 messages in Redis for context continuity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RagService ragService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiInteractionRepository interactionRepository;

    private static final int MAX_HISTORY = 10;
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    public ChatResponse chat(User user, String sessionId, String message) {
        log.info("Chat request | session={} | user={}", sessionId, user.getEmail());

        // Retrieve conversation history from Redis
        String historyKey = "chat:" + sessionId;
        List<String> history = getHistory(historyKey);

        // Build context string from history
        String sessionContext = history.isEmpty() ? null :
                String.join("\n", history.subList(Math.max(0, history.size() - 6), history.size()));

        // Call RAG pipeline
        String response = ragService.query(message, sessionContext);

        // Update history
        history.add("User: " + message);
        history.add("Assistant: " + response);
        if (history.size() > MAX_HISTORY * 2) {
            history = history.subList(history.size() - MAX_HISTORY * 2, history.size());
        }
        redisTemplate.opsForValue().set(historyKey, history, SESSION_TTL);

        // Log interaction for analytics
        persistInteraction(user, sessionId, message, response);

        return new ChatResponse(sessionId, message, response, history.size() / 2);
    }

    public List<String> getSessionHistory(String sessionId) {
        return getHistory("chat:" + sessionId);
    }

    public void clearSession(String sessionId) {
        redisTemplate.delete("chat:" + sessionId);
    }

    @SuppressWarnings("unchecked")
    private List<String> getHistory(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List<?> list) {
            return new ArrayList<>((List<String>) list);
        }
        return new ArrayList<>();
    }

    private void persistInteraction(User user, String sessionId, String query, String response) {
        try {
            AiInteraction interaction = AiInteraction.builder()
                    .userId(user.getId())
                    .sessionId(sessionId)
                    .query(query)
                    .response(response)
                    .model("gpt-4o-mini")
                    .interactionType("RAG_CHAT")
                    .tokensUsed(estimateTokens(query + response))
                    .build();
            interactionRepository.save(interaction);
        } catch (Exception e) {
            log.warn("Failed to persist AI interaction: {}", e.getMessage());
        }
    }

    private int estimateTokens(String text) {
        return text.length() / 4;
    }
}

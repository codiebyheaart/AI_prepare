package com.finsight.ai.chat;

import com.finsight.auth.User;
import com.finsight.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "RAG-powered financial AI chatbot")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a message to the AI chatbot (RAG-grounded)")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request) {

        String sessionId = request.sessionId() != null
                ? request.sessionId()
                : UUID.randomUUID().toString();

        ChatResponse response = chatService.chat(user, sessionId, request.message());
        return ResponseEntity.ok(ApiResponse.success("Response generated", response));
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "Get conversation history for a session")
    public ResponseEntity<ApiResponse<List<String>>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success("History loaded",
                chatService.getSessionHistory(sessionId)));
    }

    @DeleteMapping("/history/{sessionId}")
    @Operation(summary = "Clear conversation context for a session")
    public ResponseEntity<ApiResponse<Void>> clearHistory(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session cleared", null));
    }
}

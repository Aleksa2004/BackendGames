package com.example.games_extractor.controller;

import com.example.games_extractor.config.SecurityUtil;
import com.example.games_extractor.dto.ChatMessageRequest;
import com.example.games_extractor.dto.ChatResponse;
import com.example.games_extractor.model.ChatMessage;
import com.example.games_extractor.model.Session;
import com.example.games_extractor.service.ChatService;
import com.example.games_extractor.dto.SessionSummaryDTO;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Kreiranje nove chat sesije
    @PostMapping("/sessions")
    public ResponseEntity<Session> createSession(
            @RequestBody ChatMessageRequest request
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        String username =
                SecurityUtil.getCurrentUserDetails().getUsername();

        String tenant =
                SecurityUtil.getCurrentTenant();

        Session session = chatService.createSession(
                userId,
                username,
                tenant,
                request.getMessage()
        );

        return ResponseEntity.ok(session);
    }

    // Nastavak postojeće chat sesije
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatResponse> continueSession(
            @PathVariable Long sessionId,
            @RequestBody ChatMessageRequest request
    ) throws Exception {

        ChatResponse response =
                chatService.continueSession(
                        sessionId,
                        request.getMessage()
                );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummaryDTO>> getUserSessions() {

        Long userId = SecurityUtil.getCurrentUserId();
        String tenant = SecurityUtil.getCurrentTenant();

        List<Session> sessions =
                chatService.getUserSessions(userId, tenant);

        List<SessionSummaryDTO> result = sessions.stream()
                .map(session ->
                        new SessionSummaryDTO(
                                session.getId(),
                                session.getTitle()
                        )
                )
                .toList();

        return ResponseEntity.ok(result);
    }
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable Long sessionId
    ) {

        Long userId = SecurityUtil.getCurrentUserId();
        String tenant = SecurityUtil.getCurrentTenant();

        List<ChatMessage> messages =
                chatService.getMessages(
                        sessionId,
                        userId,
                        tenant
                );

        return ResponseEntity.ok(messages);
    }
}
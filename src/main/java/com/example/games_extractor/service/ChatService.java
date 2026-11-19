package com.example.games_extractor.service;

import com.example.games_extractor.config.SecurityUtil;
import com.example.games_extractor.dto.ChatResponse;
import com.example.games_extractor.dto.GameResponse;
import com.example.games_extractor.dto.Message;
import com.example.games_extractor.dto.SearchFilters;
import com.example.games_extractor.dto.SearchResult;
import com.example.games_extractor.model.ChatMessage;
import com.example.games_extractor.model.Game;
import com.example.games_extractor.model.MessageRole;
import com.example.games_extractor.model.Session;
import com.example.games_extractor.repository.ChatMessageRepository;
import com.example.games_extractor.repository.SessionRepository;

import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//Servis za pravljenje chat logike
@Service
public class ChatService {

    private final SessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GroqService groqService;
    private final QdrantService qdrantService;
    private final ObjectMapper mapper;

    public ChatService(
            SessionRepository sessionRepository,
            ChatMessageRepository chatMessageRepository,
            GroqService groqService,
            QdrantService qdrantService,
            ObjectMapper mapper
    ) {
        this.sessionRepository = sessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.groqService = groqService;
        this.qdrantService = qdrantService;
        this.mapper=mapper;
    }

    public Session createSession(
            Long userId,
            String username,
            String tenant,
            String firstMessage
    ) {

        Session session = new Session();

        session.setTitle(createTitle(firstMessage));
        session.setUserId(userId);
        session.setUsername(username);
        session.setTenant(tenant);

        return sessionRepository.save(session);
    }

    private String createTitle(String message) {

        int maxLength = 40;

        if (message.length() <= maxLength) {
            return message;
        }

        return message.substring(0, maxLength) + "...";
    }

    public List<ChatMessage> getMessages(
            Long sessionId,
            Long userId,
            String tenant
    ) {

        sessionRepository
                .findByIdAndUserIdAndTenant(sessionId, userId, tenant)
                .orElseThrow(() ->
                        new RuntimeException("Session not found")
                );

        return chatMessageRepository
                .findBySessionIdOrderByTimestampAsc(sessionId);
    }
    public ChatMessage saveMessage(
            Long sessionId,
            String content,
            MessageRole role,
            Long userId,
            String tenant
    ) {

        Session session = getUserSession(
                sessionId,
                userId,
                tenant
        );

        ChatMessage message = new ChatMessage();

        message.setTimestamp(LocalDateTime.now());
        message.setContent(content);
        message.setRole(role);
        message.setSession(session);

        session.getMessages().add(message);

        ChatMessage savedMessage =
                chatMessageRepository.save(message);

        sessionRepository.save(session);

        return savedMessage;
    }
    public ChatResponse continueSession(
            Long sessionId,
            String userMessage
    ) throws Exception {

        Long userId =
                SecurityUtil.getCurrentUserId();

        String tenant =
                SecurityUtil.getCurrentTenant();

        // 1. Provjeri da sesija pripada useru i tenantu
        getUserSession(
                sessionId,
                userId,
                tenant
        );

        // 2. Sačuvaj USER poruku
        saveMessage(
                sessionId,
                userMessage,
                MessageRole.USER,
                userId,
                tenant
        );

        // 3. Učitaj cijelu konverzaciju
        List<ChatMessage> chatMessages =
                getMessages(
                        sessionId,
                        userId,
                        tenant
                );
        // 3. Pretvori ChatMessage u DTO Message

        List<Message> messages = new ArrayList<>();

        for (ChatMessage message : chatMessages) {

            messages.add(
                    new Message(
                            message.getRole().name(),
                            message.getContent()
                    )
            );
        }

        // 4. Groq pravi trenutne filtere

        SearchFilters filters =
                groqService.generateCurrentQuery(messages);

        System.out.println("========== CURRENT FILTERS ==========");

        System.out.println(
                "Semantic query: "
                + filters.getSemanticQuery()
        );

        System.out.println(
                "Price: "
                + filters.getPrice()
        );

        System.out.println(
                "Age: "
                + filters.getRequiredAge()
        );

        System.out.println(
                "Windows: "
                + filters.getWindows()
        );

        System.out.println(
                "Mac: "
                + filters.getMac()
        );

        System.out.println(
                "Linux: "
                + filters.getLinux()
        );

        System.out.println(
                "Genres: "
                + filters.getGenres()
        );

        System.out.println(
                "Categories: "
                + filters.getCategories()
        );

        System.out.println(
                "Developers: "
                + filters.getDevelopers()
        );

        System.out.println(
                "Release date: "
                + filters.getReleaseDate()
        );

        System.out.println("====================================");
        
        
        System.out.println("FILTER OBJECT BEFORE QDRANT: " + filters);
        System.out.println("GENRES BEFORE QDRANT: " + filters.getGenres());
        System.out.println("CATEGORIES BEFORE QDRANT: " + filters.getCategories());
        // 5. Pretraga
       // SearchFilters testFilters = new SearchFilters();

        List<SearchResult> results =
                qdrantService.search(
                        filters.getSemanticQuery(),
                        filters
                );

        // 6. Ako nema rezultata

        if (results.isEmpty()) {

            String answer =
                    "I couldn't find any games matching your request.";

            saveMessage(
                    sessionId,
                    answer,
                    MessageRole.BOT,
                    userId,
                    tenant
            );

            return new ChatResponse(
                    answer,
                    new ArrayList<>()
            );
        }

        // 7. Groq generiše tekstualni odgovor

        String answer =
                groqService.generateChatAnswer(
                        filters.getSemanticQuery(),
                        results
                );

        // 8. Pretvori SearchResult u GameResponse

        List<GameResponse> games =
                new ArrayList<>();

        for (SearchResult result : results) {

            Game game = result.getGame();
            
            System.out.println("GAME CATEGORY: " + game.getCategory());
            
            GameResponse gameResponse =
                    new GameResponse(
                            game.getId(),
                            game.getTitle(),
                            game.getPrice(),
                            game.getReleaseDate(),
                            game.getImageUrl(),
                            game.getMetadata(),
                            game.getCategory(),
                            game.getDescription()
                          
                    );

            games.add(gameResponse);
        }

        // 9. Napravi kompletan odgovor

        ChatResponse chatResponse =
                new ChatResponse(
                        answer,
                        games
                );

        // 10. Pretvori odgovor u JSON

        String botContent =
                mapper.writeValueAsString(chatResponse);

        // 11. Sačuvaj BOT poruku

        saveMessage(
                sessionId,
                botContent,
                MessageRole.BOT,
                userId,
                tenant
        );

        return chatResponse;
    }
    public List<Message> getConversation(
            Long sessionId,
            Long userId,
            String tenant
    ) {

        List<ChatMessage> chatMessages =
                getMessages(
                        sessionId,
                        userId,
                        tenant
                );

        return chatMessages.stream()
                .map(message -> new Message(
                        message.getRole().name(),
                        message.getContent()
                ))
                .toList();
    }
    
    public List<Session> getUserSessions(
            Long userId,
            String tenant
    ) {

        return sessionRepository
                .findByUserIdAndTenantOrderByUpdatedAtDesc(
                        userId,
                        tenant
                );
    }
    
    private Session getUserSession(
            Long sessionId,
            Long userId,
            String tenant
    ) {

        return sessionRepository
                .findByIdAndUserIdAndTenant(
                        sessionId,
                        userId,
                        tenant
                )
                .orElseThrow(() ->
                        new RuntimeException("Session not found")
                );
    }
}
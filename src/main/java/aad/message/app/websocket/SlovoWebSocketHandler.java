package aad.message.app.websocket;

import aad.message.app.group.GroupService;
import aad.message.app.jwt.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SlovoWebSocketHandler extends TextWebSocketHandler {
    private final GroupService groupService;
    private final JwtUtils jwtUtils;
    private static final ConcurrentHashMap<Long, Set<WebSocketSession>> groupSessions = new ConcurrentHashMap<>();

    public SlovoWebSocketHandler(GroupService groupService, JwtUtils jwtUtils) {
        this.groupService = groupService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Optional<Long> id = validateToken(session);
        session.getAttributes().put("userId", id);
        super.afterConnectionEstablished(session);
    }

    private Optional<Long> validateToken(WebSocketSession session) {
        List<String> authHeaders = session.getHandshakeHeaders().get("authorization");

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long id = jwtUtils.validateTokenAndGetId(token, "access");

                if (id != null) {
                    // You now have the user ID, save it in session attributes or use directly
                    System.out.println("Authenticated WebSocket user ID: " + id);
                    return Optional.of(id);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if(validateToken(session).isEmpty()) {
            session.close();
        }
        System.out.println("Slovo Socket: text message received " + message.getPayload());
        String payload = message.getPayload();
        String[] parts = payload.split(" ");

        if (parts.length == 2 && "GROUP_SWITCH".equals(parts[0])) {
            Long groupId = Long.parseLong(parts[1]);

            Optional<Long> userId = (Optional<Long>) session.getAttributes().get("userId");

            if (!groupService.isUserInGroup(groupId, userId.get())) return;

            addSessionToGroup(groupId, session);
        }
    }

    private void addSessionToGroup(Long groupId, WebSocketSession session) {
        System.out.println("Adding a seession to a group. " + groupId);
        groupSessions.computeIfAbsent(groupId, k -> new HashSet<>()).add(session);
    }

    public void sendMessageToGroup(Long groupId, Long relevantId) {
        sendMessageToGroups(List.of(groupId), relevantId);
    }

    public void sendMessageToGroups(Iterable<Long> groupIds, Long relevantId) {
        System.out.println("Sending a message to a client NEW_MESSAGE " + relevantId);

        for (Long groupId : groupIds) {
            Set<WebSocketSession> sessions = groupSessions.get(groupId);
            if (sessions != null) {
                for (WebSocketSession session : sessions) {
                    try {
                        if(validateToken(session).isEmpty()) {
                            session.close();
                        }
                        session.sendMessage(new TextMessage("NEW_MESSAGE " + relevantId));
                        System.out.println("Successfully sent the message. ");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        for (Set<WebSocketSession> sessions : groupSessions.values()) {
            System.out.println("Removing a session.");
            sessions.remove(session);
        }
    }
}


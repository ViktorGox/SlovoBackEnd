package aad.message.app.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SlovoWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<Long, Set<WebSocketSession>> groupSessions = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String[] parts = payload.split(" ");

        if (parts.length == 2 && "GROUP_SWITCH".equals(parts[0])) {
            Long groupId = Long.parseLong(parts[1]);

            addSessionToGroup(groupId, session);
        }
    }

    private void addSessionToGroup(Long groupId, WebSocketSession session) {
        groupSessions.computeIfAbsent(groupId, k -> new HashSet<>()).add(session);
    }

    public void sendMessageToGroup(Long groupId) {
        sendMessageToGroups(List.of(groupId));
    }

    public void sendMessageToGroups(Iterable<Long> groupIds) {
        for (Long groupId : groupIds) {
            Set<WebSocketSession> sessions = groupSessions.get(groupId);
            if (sessions != null) {
                for (WebSocketSession session : sessions) {
                    try {
                        session.sendMessage(new TextMessage("NEW_MESSAGE"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // Optionally, remove session when disconnected
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove session from all groups it was part of
        for (Set<WebSocketSession> sessions : groupSessions.values()) {
            sessions.remove(session);
        }
    }
}


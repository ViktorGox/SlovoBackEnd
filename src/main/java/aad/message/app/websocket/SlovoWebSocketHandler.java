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
        System.out.println("Slovo Socket: text message received " + message.getPayload());
        String payload = message.getPayload();
        String[] parts = payload.split(" ");

        if (parts.length == 2 && "GROUP_SWITCH".equals(parts[0])) {
            Long groupId = Long.parseLong(parts[1]);

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


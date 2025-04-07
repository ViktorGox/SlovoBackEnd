package aad.message.app.websocket;

import aad.message.app.group.GroupService;
import aad.message.app.jwt.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GroupService groupService;
    private final JwtUtils jwtUtils;

    public WebSocketConfig(GroupService groupService, JwtUtils jwtUtils) {
        this.groupService = groupService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SlovoWebSocketHandler(groupService, jwtUtils), "/ws").setAllowedOrigins("*");
    }
}
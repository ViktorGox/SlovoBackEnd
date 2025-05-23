package aad.message.app.middleware;

import aad.message.app.jwt.JwtUtils;
import aad.message.app.user.User;
import aad.message.app.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JwtMiddleware extends OncePerRequestFilter {
    private final ApplicationContext context;
    private final JwtUtils jwtUtils;

    @Autowired
    public JwtMiddleware(ApplicationContext context, JwtUtils jwtUtils) {
        this.context = context;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return ("/users".equals(path) && "POST".equalsIgnoreCase(method)) || path.startsWith("/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        Long id = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            id = jwtUtils.validateTokenAndGetId(token, "access");
        }

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserService userService = context.getBean(UserService.class);
            Optional<User> user = userService.loadUserById(id);

            if (user.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Saves only the id in the token, rather than the whole user because
            //  The user data there can be outdated.
            //  If decoded, user information can be seen that shouldn't be seen.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.get().id, null, List.of());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
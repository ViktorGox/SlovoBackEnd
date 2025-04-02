package aad.message.app.middleware;

import aad.message.app.user.UserRegisterDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class RegisterInterceptor implements HandlerInterceptor {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("POST")) {

            if (request.getContentType().startsWith("multipart/form-data")) {
                Part dtoPart = request.getPart("dto");
                if (dtoPart != null) {
                    String dtoJson = new String(dtoPart.getInputStream().readAllBytes());

                    ObjectMapper objectMapper = new ObjectMapper();
                    UserRegisterDTO dto = objectMapper.readValue(dtoJson, UserRegisterDTO.class);

                    Collection<String> missingFields = UserRegisterDTO.verify(dto);
                    if (!missingFields.isEmpty()) {
                        sendErrorResponse(response, "Missing fields: " + String.join(", ", missingFields));
                        return false;
                    }

                    String error = validateInput(dto.username, dto.firstName, dto.lastName, dto.email, dto.password);
                    if (error != null) {
                        sendErrorResponse(response, error);
                        return false;
                    }
                } else {
                    sendErrorResponse(response, "Missing 'dto' part in the multipart request.");
                    return false;
                }
            }
        }
        return true;
    }

    private String validateInput(String username, String firstName, String lastName, String email, String password) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 1-20 characters long and cannot contain spaces.";
        }
        if (firstName == null || firstName.length() > 15) {
            return "First Name cannot exceed 15 characters.";
        }
        if (lastName != null && lastName.length() > 15) {
            return "Last Name cannot exceed 15 characters.";
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format.";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), Map.of("error", errorMessage));
    }
}

package aad.message.app.middleware;

import aad.message.app.user.UserUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class UpdateUserInterceptor implements HandlerInterceptor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("PUT")) {

            if (request.getContentType().startsWith("multipart/form-data")) {
                Part dtoPart = request.getPart("dto");

                if (dtoPart != null) {
                    String dtoJson = new String(dtoPart.getInputStream().readAllBytes());

                    ObjectMapper objectMapper = new ObjectMapper();
                    UserUpdateDTO dto = objectMapper.readValue(dtoJson, UserUpdateDTO.class);

                    String error = validateInput(dto);
                    if (error != null) {
                        sendErrorResponse(response, error);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String validateInput(UserUpdateDTO dto) {
        if (dto.firstName != null && dto.firstName.length() > 15) {
            return "First Name cannot exceed 15 characters.";
        }
        if (dto.lastName != null && dto.lastName.length() > 15) {
            return "Last Name cannot exceed 15 characters.";
        }
        if (dto.email != null && !EMAIL_PATTERN.matcher(dto.email).matches()) {
            return "Invalid email format.";
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), java.util.Map.of("error", errorMessage));
    }
}

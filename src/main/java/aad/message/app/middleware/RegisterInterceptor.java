package aad.message.app.middleware;

import aad.message.app.user.UserRegisterDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import static aad.message.app.middleware.ResponseUtil.writeErrorResponse;

@Component
public class RegisterInterceptor implements HandlerInterceptor {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("POST")) {
            String contentType = request.getContentType();

            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Content-Type. Must be 'multipart/form-data'.");
                return false;
            }

            Part dtoPart = request.getPart("dto");
            if (dtoPart != null && dtoPart.getSize() > 0) {
                String dtoJson = new String(dtoPart.getInputStream().readAllBytes());

                ObjectMapper objectMapper = new ObjectMapper();
                UserRegisterDTO dto = null;

                try {
                    dto = objectMapper.readValue(dtoJson, UserRegisterDTO.class);
                } catch (JsonProcessingException e) {
                    writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format in 'dto' part: " + e.getMessage());
                    return false;
                }

                Collection<String> missingFields = UserRegisterDTO.verify(dto);
                if (!missingFields.isEmpty()) {
                    writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing fields: " + String.join(", ", missingFields));
                    return false;
                }

                String error = validateInput(dto.username, dto.firstName, dto.lastName, dto.email, dto.password);
                if (error != null) {
                    writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
                    return false;
                }
            } else {
                writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing 'dto' part in the multipart request.");
                return false;
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
}

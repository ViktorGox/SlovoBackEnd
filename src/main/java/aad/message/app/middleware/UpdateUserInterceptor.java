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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("PUT")) {
            String contentType = request.getContentType();

            if (contentType == null) {
                sendErrorResponse(response, "Missing Content-Type header.");
                return false;
            }

            if (contentType.startsWith("multipart/form-data")) {
                Part dtoPart = request.getPart("dto");
                if (dtoPart != null && dtoPart.getSize() > 0) {
                    String dtoJson = new String(dtoPart.getInputStream().readAllBytes());
                    UserUpdateDTO dto = objectMapper.readValue(dtoJson, UserUpdateDTO.class);
                    return validateAndRespond(dto, response);
                } else {
                    sendErrorResponse(response, "Missing or empty 'dto' part in multipart request.");
                    return false;
                }

            } else if (contentType.startsWith("application/json")) {
                byte[] rawBody = request.getInputStream().readAllBytes();

                if (rawBody.length == 0) {
                    sendErrorResponse(response, "Request body is empty.");
                    return false;
                }

                UserUpdateDTO dto;
                try {
                    dto = objectMapper.readValue(rawBody, UserUpdateDTO.class);
                } catch (Exception e) {
                    sendErrorResponse(response, "Malformed JSON body.");
                    return false;
                }

                // Check if all fields are null
                if (dto.firstName == null && dto.lastName == null && dto.email == null) {
                    sendErrorResponse(response, "No user fields provided. At least one field is required.");
                    return false;
                }

                return validateAndRespond(dto, response);
            }
        }

        return true;
    }



    private boolean validateAndRespond(UserUpdateDTO dto, HttpServletResponse response) throws IOException {
        String error = validateInput(dto);
        if (error != null) {
            sendErrorResponse(response, error);
            return false;
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
        objectMapper.writeValue(response.getWriter(), java.util.Map.of("error", errorMessage));
    }
}

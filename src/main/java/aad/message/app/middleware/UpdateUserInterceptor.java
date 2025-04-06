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

import static aad.message.app.middleware.ResponseUtil.writeErrorResponse;

@Component
public class UpdateUserInterceptor implements HandlerInterceptor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("PUT")) {
            String contentType = request.getContentType();

            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Content-Type. Must be 'multipart/form-data'.");
                return false;
            }

            Part dtoPart = request.getPart("dto");
            Part filePart = request.getPart("file");

            boolean hasValidDto = false;

            if (dtoPart != null && dtoPart.getSize() > 0) {
                String dtoJson = new String(dtoPart.getInputStream().readAllBytes());
                UserUpdateDTO dto = null;

                try {
                    dto = objectMapper.readValue(dtoJson, UserUpdateDTO.class);
                } catch (Exception e) {
                    writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format in 'dto' part: " + e.getMessage());
                    return false;
                }

                if (dto.firstName != null || dto.lastName != null || dto.email != null) {
                    hasValidDto = true;

                    // Validate fields
                    return validateAndRespond(dto, response);
                }
            }

            // If there's no valid DTO, make sure at least a file is being sent
            if (!hasValidDto && (filePart == null || filePart.getSize() == 0)) {
                writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No data provided. Must include either user fields or a file.");
                return false;
            }
        }

        return true;
    }

    private boolean validateAndRespond(UserUpdateDTO dto, HttpServletResponse response) throws IOException {
        String error = validateInput(dto);
        if (error != null) {
            writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
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
}

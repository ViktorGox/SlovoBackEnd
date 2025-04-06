package aad.message.app;

import aad.message.app.middleware.RegisterInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterInterceptorTest {

    private RegisterInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        interceptor = new RegisterInterceptor();
    }

    @Test
    public void testValidRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String dtoJson = "{\"username\":\"validUser\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"password123\"}";
        request.addPart(new MockPart("dto", dtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Test when all required fields are provided
        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result); // It should allow the request
        assertEquals(200, response.getStatus()); // OK status (no error)
    }

    @Test
    public void testInvalidJsonInRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String invalidDtoJson = "{\"username\":\"validUser\", \"firstName\":\"John\", \"lastName\": }"; // Invalid JSON
        request.addPart(new MockPart("dto", invalidDtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testMissingDtoPart() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testMissingRequiredFields() throws Exception {
        // Simulate missing the email field
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String dtoJson = "{\"username\":\"validUser\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"password\":\"password123\"}"; // Missing email
        request.addPart(new MockPart("dto", dtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testInvalidUsername() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String dtoJson = "{\"username\":\"invalid username!\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"password123\"}";
        request.addPart(new MockPart("dto", dtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testInvalidEmailFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String dtoJson = "{\"username\":\"validUser\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"invalid-email\", \"password\":\"password123\"}";
        request.addPart(new MockPart("dto", dtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testShortPassword() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        String dtoJson = "{\"username\":\"validUser\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"123\"}";
        request.addPart(new MockPart("dto", dtoJson.getBytes()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }
}

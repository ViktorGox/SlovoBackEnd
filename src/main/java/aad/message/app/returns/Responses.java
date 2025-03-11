package aad.message.app.returns;

import org.springframework.http.ResponseEntity;

import java.util.Collections;

public abstract class Responses {
    public static ResponseEntity<?> Ok(String title, String value) {
        return ResponseEntity.ok().body(Collections.singletonMap(title, value));
    }

    public static ResponseEntity<?> Error(String value) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", value));
    }
}

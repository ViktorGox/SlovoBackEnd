package aad.message.app.returns;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class Responses {
    public static ResponseEntity<?> ok(String title, String value) {
        return ResponseEntity.ok().body(Collections.singletonMap(title, value));
    }

    public static ResponseEntity<?> error(String value) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", value));
    }

    public static ResponseEntity<?> internalError(String value) {
        return ResponseEntity.internalServerError().body(Collections.singletonMap("error", value));
    }

    public static ResponseEntity<?> incompleteBody(Collection<String> missingElements) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Missing fields",
                "missing_fields", missingElements
        ));
    }

    public static ResponseEntity<?> notFound(String value) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", value));
    }
}

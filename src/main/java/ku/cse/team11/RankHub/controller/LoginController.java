package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.login.AuthService;
import ku.cse.team11.RankHub.dto.auth.LoginRequest;
import ku.cse.team11.RankHub.dto.auth.LoginResponse;
import ku.cse.team11.RankHub.dto.auth.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthService authService;

    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Validated @RequestBody SignupRequest req) {
        try {
            Long id = authService.signup(req);
            return ResponseEntity.ok(Map.of("memberId", id, "message", "SIGNUP_OK"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest req) {
        try {
            LoginResponse res = authService.login(req);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        var first = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String msg = first
                .map(fe -> "Validation failed: " + fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }
}

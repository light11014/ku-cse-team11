package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.login.AuthService;
import ku.cse.team11.RankHub.dto.auth.LoginRequest;
import ku.cse.team11.RankHub.dto.auth.LoginResponse;
import ku.cse.team11.RankHub.dto.auth.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Validated @RequestBody SignupRequest req) {
        Long id = authService.signup(req);
        return ResponseEntity.ok().body(
                java.util.Map.of("memberId", id, "message", "SIGNUP_OK")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest req) {
        LoginResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

}

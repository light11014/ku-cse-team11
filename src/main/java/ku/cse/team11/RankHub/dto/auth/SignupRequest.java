package ku.cse.team11.RankHub.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String loginId,
        @NotBlank String name,
        @NotBlank String password
) {}
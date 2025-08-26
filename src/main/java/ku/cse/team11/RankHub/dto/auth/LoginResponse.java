package ku.cse.team11.RankHub.dto.auth;

public record LoginResponse(
        Long memberId,
        String loginId,
        String name,
        String message
) {}

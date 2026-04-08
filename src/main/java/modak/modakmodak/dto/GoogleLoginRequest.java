package modak.modakmodak.dto;

public record GoogleLoginRequest(
        String idToken // Google에서 발급한 ID 토큰
) {
}

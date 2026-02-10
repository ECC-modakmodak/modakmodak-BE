package modak.modakmodak.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        Long id,
        String message,
        UserData user) {
    @Builder
    public record UserData(
            Long userId,
            String username,
            String email,
            String nickname,
            String profileImage,
            String provider) {
    }
}

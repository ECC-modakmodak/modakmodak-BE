package modak.modakmodak.dto;

import java.util.List;

public record MateRequestListResponse(
        List<MateRequestDto> requests) {
    public record MateRequestDto(
            Long requestId,
            Long fromUserId,
            String fromNickname,
            String fromProfileImageUrl,
            String status,
            String createdAt) {
    }
}

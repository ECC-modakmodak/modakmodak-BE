package modak.modakmodak.dto;

import java.util.List;

public record MateListResponse(
        List<MateDto> mates) {
    public record MateDto(
            Long mateUserId,
            String nickname,
            String profileImageUrl,
            String createdAt) {
    }
}

package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 상세 장소 수정 요청")
public record LocationDetailUpdateRequest(
        @Schema(description = "변경할 상세 장소 (건물명, 호수 등)", example = "이화여대 ECC B101호")
        String locationDetail
) {}
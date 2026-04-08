package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 날짜 수정 요청")
public record DateUpdateRequest(
        @Schema(description = "변경할 날짜와 시간 (ISO 8601 형식)", example = "2026-02-25T19:00:00")
        String date
) {}
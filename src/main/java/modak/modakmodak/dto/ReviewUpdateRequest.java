package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewUpdateRequest(
        @Schema(description = "나의 집중 지수 (1~5)", example = "5")
        int focusRating,

        @Schema(description = "목표 달성 정도 (%)", example = "90")
        int goalAchievement,

        @Schema(description = "수정할 회고 내용", example = "수정: 집중이 잘 됐고 목표도 거의 달성했다.")
        String content
) {}
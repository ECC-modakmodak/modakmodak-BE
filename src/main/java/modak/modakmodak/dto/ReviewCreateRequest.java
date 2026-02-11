package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import modak.modakmodak.entity.ReviewTag;
import java.util.List;

public record ReviewCreateRequest(
        @Schema(description = "모임 ID", example = "1")
        Long meetingId,

        @Schema(description = "목표 달성 여부 (SUCCESS, PARTIAL, FAIL)", example = "SUCCESS")
        String goalStatus,

        @Schema(description = "나의 집중 지수 (1~5)", example = "4")
        int focusRating,

        @Schema(description = "자기 평가 태그 리스트", example = "[\"DONE\", \"PROUD\"]")
        List<ReviewTag> selfEvaluationTags,

        @Schema(description = "한 줄 회고 내용", example = "목표한 걸 다 끝내서 뿌듯해요.")
        String content
) {}
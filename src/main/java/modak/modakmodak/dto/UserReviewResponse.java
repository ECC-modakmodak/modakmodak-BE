package modak.modakmodak.dto;

import modak.modakmodak.entity.ReviewTag;
import java.util.List;

public record UserReviewResponse(
        Long retrospectId,      // 회고 고유 번호
        String meetingTitle,   // 모임 제목
        String goalStatus,     // 목표 달성 상태 (O, Δ, X)
        int focusRating,       // 집중 지수 (1~5)
        List<ReviewTag> tags,     // 선택한 태그들
        String content,        // 회고 내용
        String createdAt       // 작성 시간 (최신순 정렬용)
) {}
package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.ReviewCreateRequest;
import modak.modakmodak.dto.UserReviewResponse;
import modak.modakmodak.entity.Meeting;
import modak.modakmodak.entity.Review;
import modak.modakmodak.entity.User;
import modak.modakmodak.meeting.MeetingRepository;
import modak.modakmodak.repository.ReviewRepository;
import modak.modakmodak.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import modak.modakmodak.dto.ReviewUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "모임 회고 관련 API")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Operation(summary = "내 회고 목록 조회 (최신순)", description = "프로필 페이지에서 최신순으로 회고를 모아봅니다.")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserReviewResponse>> getUserReviews(@PathVariable String username) {
        // Repository에서 만든 최신순 정렬 메서드를 호출합니다.
        List<Review> reviews = reviewRepository.findByUserUsernameOrderByCreatedAtDesc(username);

        List<UserReviewResponse> response = reviews.stream()
                .map(r -> new UserReviewResponse(
                        r.getId(),
                        r.getMeeting().getTitle(),
                        r.getGoalStatus(),
                        r.getFocusRating(),
                        r.getSelfEvaluationTags(),
                        r.getContent(),
                        r.getCreatedAt().toString()
                )).toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회고 작성", description = "모임 종료 후 나의 목표 달성도와 회고를 기록합니다.")
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReviewCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findById(request.meetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        Review review = Review.builder()
                .user(user)
                .meeting(meeting)
                .goalStatus(request.goalStatus())
                .focusRating(request.focusRating())
                .selfEvaluationTags(request.selfEvaluationTags())
                .content(request.content())
                .build();

        reviewRepository.save(review);
        return ResponseEntity.ok("회고가 성공적으로 등록되었습니다.");
    }

    @Operation(summary = "회고 수정", description = "작성한 회고를 수정합니다. 회고가 없으면 안내 메시지를 반환합니다.")
    @PutMapping("/{retrospectId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long retrospectId,
            @RequestBody ReviewUpdateRequest request) {

        // 1. 수정할 회고가 있는지 확인합니다.
        Review review = reviewRepository.findById(retrospectId).orElse(null);

        // 2. 회고가 없는 경우, 에러 대신 부드러운 메시지를 보냅니다.
        if (review == null) {
            return ResponseEntity.ok("해당 모임에 작성된 회고가 없어 수정할 수 없습니다.");
        }

        // 3. 데이터 업데이트 (목표 달성 상태는 String으로 저장 중이므로 변환하여 저장하거나 필드를 추가해야 합니다)
        review.setFocusRating(request.focusRating());
        review.setContent(request.content());
        // 만약 goalStatus 필드에 %를 저장하고 싶다면 아래처럼 처리합니다.
        review.setGoalStatus(request.goalAchievement() + "%");

        reviewRepository.save(review);

        // 4. 성공 응답 양식에 맞춰 반환합니다.
        return ResponseEntity.ok(java.util.Map.of(
                "retrospectId", retrospectId,
                "message", "회고 수정 완료"
        ));
    }
}
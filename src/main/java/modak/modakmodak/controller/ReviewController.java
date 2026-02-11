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
}
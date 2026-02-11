package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 시간을 자동으로 기록해주는 리스너입니다.
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String goalStatus; // SUCCESS(O), PARTIAL(Δ), FAIL(X)
    private int focusRating; // 1~5 집중 지수

    @ElementCollection(targetClass = ReviewTag.class) // ReviewTag Enum을 사용한다고 알려줍니다
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Enumerated(EnumType.STRING) // DB에 Enum의 이름(예: DONE, PROUD)으로 저장합니다
    private List<ReviewTag> selfEvaluationTags; // 이제 중복 선택된 태그들이 Enum 리스트로 저장됩니다!

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 회고가 작성된 시간
}
package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 참여자 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting; // 어느 모임인지

    private String goal; // 시안에 있던 "빨간색 목표 태그" 내용

    @Enumerated(EnumType.STRING)
    private ReactionEmoji reactionEmoji; // 시안 하단에 있던 리액션 이모지

    private boolean isHost; // 방장 여부

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status; // APPROVED, PENDING 등

    public void updateStatus(ParticipationStatus status) {
        this.status = status;
    }

    private String statusBadge; // "집중하고 있어요", "도착했어요" 등 상태 메시지

    public void updateStatusBadge(String statusBadge) {
        this.statusBadge = statusBadge;
    }

    private Boolean attended; // 출석 여부 (null: 미체크, true: 출석, false: 결석)

    public void updateAttendance(Boolean attended) {
        this.attended = attended;
    }
}
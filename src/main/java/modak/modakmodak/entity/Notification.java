package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림 받을 사용자

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private NotificationType type;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 200, nullable = false)
    private String message;

    @Column(name = "related_id")
    private Long relatedId; // 관련 엔티티 ID (초대ID/요청ID/모임ID 등)

    @Column(name = "application_id")
    private Long applicationId; // 참여 신청 ID (Participant ID)

    @Column(name = "sender_nickname", length = 50)
    private String senderNickname; // 알림을 발생시킨 사용자의 닉네임

    @Column(name = "pod_name", length = 100)
    private String podName; // 관련 팟(모임)의 이름

    @Column(name = "profile_image", length = 500)
    private String profileImage; // 알림을 발생시킨 사용자의 프로필 이미지

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void markAsRead() {
        this.isRead = true;
    }
}

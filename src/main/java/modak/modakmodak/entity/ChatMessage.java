package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long meetingId; //어느 팟의 채팅인지

    private Long senderId; //보낸 사람 id
    private String senderNickname; //보낸 사람 닉네임

    private boolean isHost;

    @Column(columnDefinition = "TEXT")
    private String message; //채팅내용

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

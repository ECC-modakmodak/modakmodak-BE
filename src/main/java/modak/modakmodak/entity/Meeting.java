package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import modak.modakmodak.dto.MeetingDetailRequest;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate // 생성 시 자동으로 시간이 저장됩니다.
    @Column(updatable = false) // 생성 후에는 수정되지 않도록 설정
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private MeetingAtmosphere atmosphere; // 도란도란, 조용한

    @Enumerated(EnumType.STRING)
    private MeetingCategory category; // 카공, 줌공, 기타
    private String categoryEtc; // 기타

    private int maxParticipants;

    private String title;
    private LocalDateTime date;
    private String area;
    private String locationDetail;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;

    private String status; // PENDING(대기), OPEN(개설완료)

    // 세부 정보 업데이트 메서드
    public void updateDetails(MeetingDetailRequest request) {
        this.title = request.title();
        this.date = LocalDateTime.parse(request.date()); // ISO_LOCAL_DATE_TIME 형식 기준
        this.area = request.area();
        this.locationDetail = request.locationDetail();
        this.description = request.description();
        this.imageUrl = request.imageUrl();
        this.status = "OPEN";
    }
}
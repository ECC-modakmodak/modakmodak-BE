package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import modak.modakmodak.dto.MeetingDetailRequest;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
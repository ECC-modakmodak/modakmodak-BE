package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import modak.modakmodak.dto.MeetingDetailRequest;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate // 생성 시 자동으로 시간이 저장됩니다.
    @Column(updatable = false) // 생성 후에는 수정되지 않도록 설정
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

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

    @Column(columnDefinition = "TEXT")
    private String hostAnnouncement;

    @Builder.Default
    private Boolean isCompleted = false; // 팟 종료 상태

    private String status; // PENDING(대기), OPEN(개설완료)

    // 세부 정보 업데이트 메서드
    public void updateDetails(MeetingDetailRequest request) {
        this.title = request.title();
        if (request.date() != null) {
            this.date = java.time.LocalDateTime.parse(request.date());
        }
        this.area = request.area();
        this.locationDetail = request.locationDetail();
        this.description = request.description();
        this.imageUrl = (request.imageUrl() != null && !request.imageUrl().isBlank())
                ? request.imageUrl()
                : "pod_1.png";
        this.hostAnnouncement = request.hostAnnouncement();
        this.status = "OPEN";

        // 2단계에서 값이 넘어오지 않으면(null/0) 기존 1단계 설정값 유지
        if (request.atmosphere() != null) {
            this.atmosphere = request.atmosphere();
        }
        if (request.category() != null) {
            this.category = request.category();
        }
        if (request.categoryEtc() != null) {
            this.categoryEtc = request.categoryEtc();
        }
        if (request.maxParticipants() > 0) {
            this.maxParticipants = request.maxParticipants();
        }
    }

    // 팟 상세 정보 부분 수정을 위한 메서드 추가
    public void updateDetail(modak.modakmodak.dto.MeetingUpdateDetailRequest request) {
        if (request.title() != null)
            this.title = request.title();
        if (request.date() != null)
            this.date = LocalDateTime.parse(request.date());
        if (request.area() != null)
            this.area = request.area();
        if (request.locationDetail() != null)
            this.locationDetail = request.locationDetail();
        if (request.description() != null)
            this.description = request.description();
        if (request.imageUrl() != null && !request.imageUrl().equals("string") && !request.imageUrl().isBlank()) {
            this.imageUrl = request.imageUrl();
        }
        if (request.hostAnnouncement() != null)
            this.hostAnnouncement = request.hostAnnouncement();

        // 초기 세팅 정보 (atmosphere, category 등 에러 해결 부분)
        if (request.atmosphere() != null)
            this.atmosphere = request.atmosphere();
        if (request.category() != null)
            this.category = request.category();
        if (request.categoryEtc() != null)
            this.categoryEtc = request.categoryEtc();
        if (request.maxParticipants() > 0)
            this.maxParticipants = request.maxParticipants();
    }

    // 팟 종료 메서드
    public void completeMeeting() {
        this.isCompleted = true;
    }

    public void setHostAnnouncement(String hostAnnouncement) {
        this.hostAnnouncement = hostAnnouncement;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }

    public void setAtmosphere(MeetingAtmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    public void setCategory(MeetingCategory category) {
        this.category = category;
    }

    public void setCategoryEtc(String categoryEtc) {
        this.categoryEtc = categoryEtc;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
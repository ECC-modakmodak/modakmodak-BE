package modak.modakmodak.entity;

import jakarta.persistence.*;
import lombok.*;
import modak.modakmodak.dto.UserProfileRequest;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true) // OAuth 사용자는 비밀번호 없음
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    @Builder.Default
    private String provider = "LOCAL"; // "LOCAL" 또는 "GOOGLE"

    @Column(length = 255)
    private String providerId; // OAuth 제공자의 고유 사용자 ID

    private String nickname;
    private String profileImage;
    private String activityArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_type", columnDefinition = "VARCHAR(255)") // 길이를 255로 강제 지정
    private MeetingAtmosphere preferredType;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_method", columnDefinition = "VARCHAR(255)") // 길이를 255로 강제 지정
    private MeetingMethod preferredMethod;

    @Builder.Default
    @Column(name = "attendance_rate", nullable = false)
    private Float attendanceRate = 0.0f;

    private String statusMessage;
    private String targetMessage;

    // 회원가입 전용 생성자
    public User(String username, String password, String email, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }

    public void updateProfile(UserProfileRequest request) {
        if (request.nickname() != null)
            this.nickname = request.nickname();
        if (request.email() != null)
            this.email = request.email();
        if (request.profileImage() != null)
            this.profileImage = request.profileImage();
        if (request.targetMessage() != null)
            this.targetMessage = request.targetMessage();
        if (request.activityArea() != null)
            this.activityArea = request.activityArea();
    }
}
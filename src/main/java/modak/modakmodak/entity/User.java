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

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;
    private String profileImage;
    private String activityArea;

    @Enumerated(EnumType.STRING)
    private MeetingAtmosphere preferredType;

    @Enumerated(EnumType.STRING)
    private MeetingMethod preferredMethod;

    @Builder.Default
    private float attendanceRate = 0;

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
        if (request.nickname() != null) this.nickname = request.nickname();
        if (request.email() != null) this.email = request.email();
        if (request.profileImage() != null) this.profileImage = request.profileImage();
        if (request.targetMessage() != null) this.targetMessage = request.targetMessage();
        if (request.preferredType() != null) this.preferredType = request.preferredType();
        if (request.activityArea() != null) this.activityArea = request.activityArea();
    }
}
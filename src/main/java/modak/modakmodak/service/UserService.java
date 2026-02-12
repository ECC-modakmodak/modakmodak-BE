package modak.modakmodak.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.LoginResponse;
import modak.modakmodak.dto.UserJoinRequest;
import modak.modakmodak.entity.User;
import modak.modakmodak.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // ◀ 이제 인텔리제이가 이 어노테이션을 인식합니다.
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GoogleOAuthService googleOAuthService;

    public Long join(UserJoinRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        userRepository.findByEmail(request.email())
                .ifPresent(u -> {
                    throw new IllegalStateException("이미 가입된 이메일입니다.");
                });

        // 2. 새로운 유저 엔티티 생성 및 저장
        User user = User.builder()
                .username(request.username())
                .password(request.password()) // ⚠️ 추후 암호화 로직 추가 권장
                .email(request.email())
                .nickname(request.nickname())
                .attendanceRate(0) // 초기 참여율 설정
                .preferredType(request.preferredType()) // 선호 분위기 (Enum)
                .preferredMethod(request.preferredMethod()) // 대면/비대면 (Enum)
                .activityArea(request.activityArea()) // 활동 지역
                .targetMessage(request.targetMessage()) // 나의 목표
                .profileImage("https://default-image.png")
                .build();

        return userRepository.save(user).getId(); // ◀ DB에 실제 저장되는 지점
    }

    @Transactional(readOnly = true)
    public boolean checkUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean checkNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * Google OAuth 로그인 처리
     * 
     * @param idToken Google ID 토큰
     * @return LoginResponse
     */
    public LoginResponse loginWithGoogle(String idToken) {
        try {
            // 1. Google ID 토큰 검증
            GoogleIdToken.Payload payload = googleOAuthService.verifyToken(idToken);

            String email = googleOAuthService.getEmail(payload);
            String providerId = googleOAuthService.getProviderId(payload);
            String name = googleOAuthService.getName(payload);
            String pictureUrl = googleOAuthService.getPictureUrl(payload);

            // 2. 기존 사용자 조회 (provider + providerId로)
            User user = userRepository.findByProviderAndProviderId("GOOGLE", providerId)
                    .orElseGet(() -> {
                        // 3. 신규 사용자인 경우 자동 회원가입
                        User newUser = User.builder()
                                .username(email.split("@")[0] + "_google_" + System.currentTimeMillis()) // 고유한 username
                                                                                                         // 생성
                                .email(email)
                                .nickname(name != null ? name : "구글사용자")
                                .profileImage(pictureUrl)
                                .provider("GOOGLE")
                                .providerId(providerId)
                                .attendanceRate(0)
                                .build();

                        return userRepository.save(newUser);
                    });

            // 4. LoginResponse 생성
            return LoginResponse.builder()
                    .id(user.getId())
                    .message("로그인 성공")
                    .user(LoginResponse.UserData.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .profileImage(user.getProfileImage())
                            .provider(user.getProvider())
                            .build())
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Google 로그인 실패: " + e.getMessage());
        }
    }
}
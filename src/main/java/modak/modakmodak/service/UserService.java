package modak.modakmodak.service;

// 아래 import 문들이 반드시 있어야 에러가 사라집니다!
import lombok.RequiredArgsConstructor;
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

    public Long join(UserJoinRequest request) {
        // 1. 이미 가입된 이메일인지 리포지토리로 확인
        userRepository.findByEmail(request.email())
                .ifPresent(u -> { throw new IllegalStateException("이미 가입된 이메일입니다."); });

        // 2. 새로운 유저 엔티티 생성 및 저장
        User user = User.builder()
                .username(request.username())
                .password(request.password()) // ⚠️ 추후 암호화 로직 추가 권장
                .email(request.email())
                .nickname(request.nickname())
                .attendanceRate(0) // 초기 참여율 설정
                .build();

        return userRepository.save(user).getId(); // ◀ DB에 실제 저장되는 지점
    }
}
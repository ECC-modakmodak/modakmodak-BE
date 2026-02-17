package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.UserJoinRequest;
import modak.modakmodak.entity.User;
import modak.modakmodak.repository.UserRepository;
import modak.modakmodak.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import modak.modakmodak.dto.UserProfileRequest;
import modak.modakmodak.dto.UserLoginRequest;
import modak.modakmodak.dto.FindIdRequest;
import modak.modakmodak.dto.FindPwRequest;
import modak.modakmodak.dto.ResetPwRequest;
import modak.modakmodak.dto.WithdrawRequest;
import modak.modakmodak.dto.UserLoginRequest;
import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User", description = "회원 관련 API (DB 연동)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // 1. 회원가입
    @Operation(summary = "회원가입", description = "새로운 유저 정보를 DB에 저장합니다.")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserJoinRequest request) {
        // 보안을 위해 가입 시점에도 중복 체크 유지
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 닉네임입니다.");
        }

        // 서비스의 join 로직을 사용하면 사진 로직까지 한 번에 처리됩니다.
        Long savedUserId = userService.join(request);
        return ResponseEntity.ok("회원가입 성공! ID:" + savedUserId);
    }

    // 2. 로그인
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 DB 데이터와 대조합니다.")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.username());
        if (user.isPresent() && user.get().getPassword().equals(request.password())) {
            return ResponseEntity.ok(user.get().getNickname() + "님, 로그인 성공!");
        }
        return ResponseEntity.status(401).body("아이디 또는 비밀번호가 틀렸습니다.");
    }

    // 2-1. Google OAuth 로그인
    @Operation(summary = "Google 로그인", description = "Google ID 토큰으로 로그인합니다.")
    @PostMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody modak.modakmodak.dto.GoogleLoginRequest request) {
        try {
            modak.modakmodak.dto.LoginResponse response = userService.loginWithGoogle(request.idToken());
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "error", "INVALID_TOKEN",
                    "message", e.getMessage()));
        }
    }

    // 3. 아이디 찾기 (이메일로 조회)
    @Operation(summary = "아이디 찾기", description = "이메일로 가입된 유저의 아이디를 찾습니다.")
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody FindIdRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());
        return user.map(u -> ResponseEntity.ok("찾으시는 아이디는: " + u.getUsername()))
                .orElse(ResponseEntity.status(404).body("해당 이메일로 가입된 정보가 없습니다."));
    }

    // 4. 임시 비밀번호 발급
    @Operation(summary = "비밀번호 찾기", description = "임시 비밀번호를 생성하여 DB를 업데이트합니다.")
    @PostMapping("/find-pw")
    public ResponseEntity<String> findPassword(@RequestBody FindPwRequest request) {

        Optional<User> user = userRepository.findByUsername(request.username());

        if (user.isPresent() && user.get().getEmail().equals(request.email())) {
            // 실제로는 무작위 문자열을 생성해야 하지만, 테스트용으로 고정합니다.
            String tempPassword = "temp1234!";
            user.get().setPassword(tempPassword); // 비밀번호 변경
            userRepository.save(user.get()); // DB 업데이트

            return ResponseEntity.ok("임시 비밀번호가 발송되었습니다: " + tempPassword);
        } else {
            return ResponseEntity.status(404).body("사용자 정보가 일치하지 않습니다.");
        }
    }

    // 5. 비밀번호 재설정
    @Operation(summary = "비밀번호 변경", description = "새로운 비밀번호를 DB에 저장합니다.")
    @PostMapping("/reset-pw")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPwRequest request) {
        Optional<User> user = userRepository.findByUsername(request.username());

        // 1. 사용자가 존재하는지 확인
        if (user.isPresent()) {
            User foundUser = user.get();

            // 2. 현재 비밀번호가 일치하는지 확인 (보안을 위한 필수 절차)
            if (foundUser.getPassword().equals(request.oldPassword())) {
                // 새 비밀번호 유효성 검증
                modak.modakmodak.util.PasswordValidator.validate(request.newPassword());

                foundUser.setPassword(request.newPassword()); // 새로운 비밀번호 세팅
                userRepository.save(foundUser); // DB 업데이트
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(401).body("현재 비밀번호가 일치하지 않습니다.");
            }
        } else {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
    }

    // 6. 회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "사용자 데이터를 DB에서 영구 삭제합니다.")
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        Optional<User> user = userRepository.findByUsername(request.username());

        // 1. 사용자가 존재하는지 확인
        if (user.isPresent()) {
            User foundUser = user.get();

            // 2. 비밀번호가 일치하는지 확인 (본인 확인)
            if (foundUser.getPassword().equals(request.password())) {
                userRepository.delete(foundUser); // DB에서 해당 데이터 삭제
                return ResponseEntity.ok("회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.");
            } else {
                return ResponseEntity.status(401).body("비밀번호가 일치하지 않아 탈퇴할 수 없습니다.");
            }
        } else {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }

    }

    @Operation(summary = "프로필 수정", description = "사용자의 닉네임, 이미지, 메시지 등을 변경합니다.")
    @PatchMapping("/profile/{userId}") // ◀ 일부 정보만 수정할 때는 PATCH를 주로 씁니다.
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable String userId,
            @RequestBody UserProfileRequest request) {

        Optional<User> userOptional = userRepository.findByUsername(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.updateProfile(request); // ◀ 엔티티의 업데이트 메서드 호출
            userRepository.save(user); // ◀ DB 저장

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "프로필 정보가 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "message", "사용자를 찾을 수 없습니다."));
        }
    }

    @Operation(summary = "프로필 조회", description = "사용자의 프로필 정보를 가져옵니다.")
    @GetMapping("/profile/{username}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String username) {

        // 1. DB에서 사용자 아이디로 정보를 찾습니다.
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 2. 시안 명세서에 맞는 보따리(Map)를 만들어 데이터를 채웁니다.
            Map<String, Object> data = new HashMap<>(); // ◀ Map.of 대신 HashMap 사용 추천

            data.put("id", user.getId()); // ◀ [추가] 모임 생성(X-User-Id)에 꼭 필요한 숫자 ID!
            data.put("userId", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("email", user.getEmail());
            data.put("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "https://...");
            data.put("attendanceRate", user.getAttendanceRate());
            data.put("targetMessage", user.getTargetMessage() != null ? user.getTargetMessage() : "");
            data.put("activityArea", user.getActivityArea() != null ? user.getActivityArea() : "");

            // ---  해시태그 리스트 만들기 ---
            List<String> hashtags = new ArrayList<>();

            if (user.getPreferredType() != null) {
                hashtags.add(user.getPreferredType().name());
            }

            if (user.getPreferredMethod() != null) {
                hashtags.add(user.getPreferredMethod().name());
            }

            data.put("hashtags", hashtags); // ◀ ["CHATTY", "대면"] 형태로 들어감

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "프로필 조회 성공",
                    "data", data));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "message", "사용자를 찾을 수 없습니다."));
        }
    }

    // 아이디 중복 확인 (프론트엔드 실시간 체크용)
    @Operation(summary = "아이디 중복 확인", description = "입력한 아이디가 이미 존재하는지 확인합니다.")
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean isAvailable = userService.checkUsernameAvailable(username);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "isAvailable", isAvailable,
                "message", isAvailable ? "사용 가능한 아이디입니다." : "이미 존재하는 아이디입니다."
        ));
    }

    // 닉네임 중복 확인 (프론트엔드 실시간 체크용)
    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임이 이미 존재하는지 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.checkNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "isAvailable", isAvailable,
                "message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 존재하는 닉네임입니다."
        ));
    }

    // 이메일 중복 확인
    @Operation(summary = "이메일 중복 확인", description = "입력한 이메일이 이미 존재하는지 확인합니다.")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.checkEmailAvailable(email);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "isAvailable", isAvailable,
                "message", isAvailable ? "사용 가능한 이메일입니다." : "이미 가입된 이메일입니다."
        ));
    }
}
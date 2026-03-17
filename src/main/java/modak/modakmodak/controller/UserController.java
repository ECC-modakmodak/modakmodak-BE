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
import modak.modakmodak.dto.UserLoginResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import modak.modakmodak.service.EmailService;
import modak.modakmodak.dto.EmailSendRequest;

@Tag(name = "User", description = "회원 관련 API (DB 연동)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

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
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) { // <?>로 변경
        Optional<User> user = userRepository.findByUsername(request.username());

        if (user.isPresent() && user.get().getPassword().equals(request.password())) {
            User loginUser = user.get();

            return ResponseEntity.ok(new UserLoginResponse(
                    loginUser.getUsername(),
                    loginUser.getNickname(),
                    "로그인 성공!"));
        }

        // 실패 시에는 기존처럼 에러 메시지 전송
        return ResponseEntity.status(401).body("아이디 또는 비밀번호가 틀렸습니다.");
    }

    // 2-1. Google OAuth 로그인
    @Operation(summary = "Google 로그인", description = "Google ID 토큰으로 로그인합니다.")
    @PostMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody modak.modakmodak.dto.GoogleLoginRequest request) {
        try {
            var result = userService.loginWithGoogle(request.idToken());

            return ResponseEntity.status(201).body(new UserLoginResponse(
                    result.user().username(), // 구글 유저 username
                    result.user().nickname(), // 구글 유저 닉네임
                    "구글 로그인 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "error", "INVALID_TOKEN",
                    "message", e.getMessage()));
        }
    }

    // 인증번호 이메일 발송
    @Operation(summary = "이메일 인증번호 발송", description = "아이디/비밀번호 찾기를 위해 이메일로 인증번호를 보냅니다.")
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailSendRequest request) {
        try {
            emailService.sendVerificationCode(request.email());
            return ResponseEntity.ok("인증번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 1. 아이디 찾기용 인증번호 발송 (닉네임 + 이메일 DB 대조)
    @Operation(summary = "아이디 찾기 인증번호 발송", description = "닉네임과 이메일이 일치하는 회원에게만 인증번호를 발송합니다.")
    @PostMapping("/find-id/send-code")
    public ResponseEntity<String> sendCodeForFindId(@RequestBody modak.modakmodak.dto.FindIdRequest request) { // ◀ 여기 이름 수정됨!
        // DB에서 닉네임과 이메일로 가입된 유저가 있는지 확인
        Optional<User> user = userRepository.findByNicknameAndEmail(request.nickname(), request.email());

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("입력하신 정보와 일치하는 회원이 없습니다."); // 시안의 '아이디 없을 경우' 처리용
        }

        try {
            // 회원이 존재하면 해당 이메일로 발송
            emailService.sendVerificationCode(user.get().getEmail());
            return ResponseEntity.ok("인증번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 2. 인증번호 확인 및 아이디/가입일 반환
    @Operation(summary = "아이디 찾기 검증 및 결과 반환", description = "인증번호를 검증한 뒤 아이디와 가입일을 반환합니다.")
    @PostMapping("/find-id/verify")
    public ResponseEntity<?> verifyAndFindId(@RequestBody modak.modakmodak.dto.FindIdVerifyRequest request) { // ◀ 여기는 그대로 유지!

        // 1. 보안을 위해 다시 한번 유저 정보 확인
        Optional<User> user = userRepository.findByNicknameAndEmail(request.nickname(), request.email());
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("정보와 일치하는 아이디가 없습니다.");
        }

        // 2. 인증번호 검증
        if (!emailService.verifyCode(request.email(), request.code())) {
            return ResponseEntity.status(401).body("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 3. 시안에 맞게 아이디(username)와 가입일(createdAt)을 반환
        User foundUser = user.get();
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "username", foundUser.getUsername(),
                "createdAt", foundUser.getCreatedAt() != null ? foundUser.getCreatedAt().toString() : "가입일 정보 없음"
        ));
    }

    // 4-1. 비밀번호 찾기용 인증번호 발송
    @Operation(summary = "비밀번호 찾기 인증번호 발송", description = "아이디와 이메일이 일치하는 회원에게 인증번호를 발송합니다.")
    @PostMapping("/find-pw/send-code")
    public ResponseEntity<String> sendCodeForFindPw(@RequestBody modak.modakmodak.dto.FindPwRequest request) {

        Optional<User> user = userRepository.findByUsernameAndEmail(request.username(), request.email());

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("입력하신 정보와 일치하는 회원이 없습니다.");
        }

        try {
            emailService.sendVerificationCode(user.get().getEmail());
            return ResponseEntity.ok("인증번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 4-2. 비밀번호 찾기용 인증번호 검증
    @Operation(summary = "비밀번호 찾기 검증", description = "이메일로 받은 인증번호가 맞는지 확인합니다.")
    @PostMapping("/find-pw/verify")
    public ResponseEntity<String> verifyForFindPw(@RequestBody modak.modakmodak.dto.FindPwVerifyRequest request) {

        // 유저 정보 재확인
        Optional<User> user = userRepository.findByUsernameAndEmail(request.username(), request.email());
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("정보와 일치하는 아이디가 없습니다.");
        }

        // 인증번호 검증
        if (!emailService.verifyCode(request.email(), request.code())) {
            return ResponseEntity.status(401).body("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok("인증에 성공했습니다. 새 비밀번호를 설정해주세요.");
    }

    // 4-3. 새 비밀번호 재설정
    @Operation(summary = "새 비밀번호 설정", description = "인증 완료 후 새로운 비밀번호로 변경합니다.")
    @PostMapping("/find-pw/reset")
    public ResponseEntity<String> resetPwAfterFind(@RequestBody modak.modakmodak.dto.FindPwResetRequest request) {

        Optional<User> user = userRepository.findByUsername(request.username());

        if (user.isPresent()) {
            User foundUser = user.get();

            // 새 비밀번호 유효성 검증 (기존에 있던 Validator 활용)
            modak.modakmodak.util.PasswordValidator.validate(request.newPassword());

            // 비밀번호 변경 및 저장
            foundUser.setPassword(request.newPassword()); // 추후 실제 서비스 시에는 암호화(Bcrypt 등) 적용 필요
            userRepository.save(foundUser);

            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 로그인해주세요.");
        } else {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
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
            data.put("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "profile_default.png");
            data.put("attendanceRate", user.getAttendanceRate());
            data.put("targetMessage", user.getTargetMessage() != null ? user.getTargetMessage() : "");
            data.put("activityArea", user.getActivityArea() != null ? user.getActivityArea() : "");

            // --- 해시태그 리스트 만들기 ---
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
                "message", isAvailable ? "사용 가능한 아이디입니다." : "이미 존재하는 아이디입니다."));
    }

    // 닉네임 중복 확인 (프론트엔드 실시간 체크용)
    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임이 이미 존재하는지 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.checkNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "isAvailable", isAvailable,
                "message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 존재하는 닉네임입니다."));
    }

    // 유저 ID 조회 (username으로 숫자 ID 가져오기) - 기존 메서드 유지

    @Operation(summary = "내 ID 조회 (Long)", description = "username으로 사용자의 숫자 ID를 반환합니다. (값만 반환)")
    @GetMapping("/me/id")
    public ResponseEntity<Long> getMyId(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(user.getId());
    }

    // 유저 ID 조회 (username으로 숫자 ID 가져오기)
    @Operation(summary = "유저 ID 조회", description = "username으로 사용자의 숫자 ID를 조회합니다.")
    @GetMapping("/id")
    public ResponseEntity<Map<String, Object>> getUserId(@RequestParam String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of("id", user.get().getId()));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "message", "사용자를 찾을 수 없습니다."));
        }
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
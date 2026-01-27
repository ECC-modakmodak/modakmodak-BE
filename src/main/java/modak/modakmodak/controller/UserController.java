package modak.modakmodak.controller;

import modak.modakmodak.entity.User;
import modak.modakmodak.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("회원가입 성공!");
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        Optional<User> user = userRepository.findByUsername(request.get("username"));
        if (user.isPresent() && user.get().getPassword().equals(request.get("password"))) {
            return ResponseEntity.ok(user.get().getNickname() + "님, 로그인 성공!");
        }
        return ResponseEntity.status(401).body("아이디 또는 비밀번호가 틀렸습니다.");
    }

    // 3. 아이디 찾기 (이메일로 조회)
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody Map<String, String> request) {
        Optional<User> user = userRepository.findByEmail(request.get("email"));
        return user.map(u -> ResponseEntity.ok("찾으시는 아이디는: " + u.getUsername()))
                .orElse(ResponseEntity.status(404).body("해당 이메일로 가입된 정보가 없습니다."));
    }

    @PostMapping("/find-pw")
    public ResponseEntity<String> findPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getEmail().equals(email)) {
            // 실제로는 무작위 문자열을 생성해야 하지만, 테스트용으로 고정합니다.
            String tempPassword = "temp1234!";
            user.get().setPassword(tempPassword); // 비밀번호 변경
            userRepository.save(user.get()); // DB 업데이트

            return ResponseEntity.ok("임시 비밀번호가 발송되었습니다: " + tempPassword);
        } else {
            return ResponseEntity.status(404).body("사용자 정보가 일치하지 않습니다.");
        }
    }

    @PostMapping("/reset-pw")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword"); // 현재(혹은 임시) 비밀번호
        String newPassword = request.get("newPassword"); // 새로 바꿀 비밀번호

        Optional<User> user = userRepository.findByUsername(username);

        // 1. 사용자가 존재하는지 확인
        if (user.isPresent()) {
            User foundUser = user.get();

            // 2. 현재 비밀번호가 일치하는지 확인 (보안을 위한 필수 절차)
            if (foundUser.getPassword().equals(oldPassword)) {
                foundUser.setPassword(newPassword); // 새로운 비밀번호 세팅
                userRepository.save(foundUser); // DB 업데이트
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(401).body("현재 비밀번호가 일치하지 않습니다.");
            }
        } else {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
    }
}
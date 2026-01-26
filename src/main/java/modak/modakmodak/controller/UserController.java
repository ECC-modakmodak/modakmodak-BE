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
}
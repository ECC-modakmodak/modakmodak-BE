package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.EmailRequest;
import modak.modakmodak.dto.EmailVerifyRequest;
import modak.modakmodak.service.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Email", description = "이메일 인증 관련 API")
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "인증번호 발송", description = "입력한 이메일로 6자리 인증번호를 발송합니다.")
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody EmailRequest request) {
        try {
            emailVerificationService.sendVerificationCode(request.email());
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "인증 메일이 발송되었습니다. 5분 이내에 입력해주세요."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "메일 발송에 실패했습니다. 이메일 주소를 다시 확인해주세요."));
        }
    }

    @Operation(summary = "인증번호 확인", description = "발송된 인증번호가 맞는지 확인합니다.")
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody EmailVerifyRequest request) {
        boolean isValid = emailVerificationService.verifyCode(request.email(), request.code());

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", "인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }
}

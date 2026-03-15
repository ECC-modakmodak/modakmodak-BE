package modak.modakmodak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    // 이메일과 인증번호를 임시 저장하는 Map (실무에서는 만료 시간이 있는 Redis 캐시 사용을 권장합니다)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 1. 인증번호 생성 및 메일 발송
    public void sendVerificationCode(String email) {
        // 6자리 난수 생성
        String code = String.format("%06d", new Random().nextInt(1000000));
        verificationCodes.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[모닥모닥] 아이디/비밀번호 찾기 인증번호 안내");
        message.setText("요청하신 인증번호는 [" + code + "] 입니다.\n화면에 입력해 주세요.");

        mailSender.send(message);
    }

    // 2. 인증번호 검증
    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(email); // 인증 성공 시 메모리에서 삭제
            return true;
        }
        return false;
    }
}
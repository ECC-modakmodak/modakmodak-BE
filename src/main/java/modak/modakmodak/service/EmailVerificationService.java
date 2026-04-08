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
public class EmailVerificationService {

    private final JavaMailSender mailSender;

    // 이메일: 인증코드, 이메일: 만료시간(ms) 저장용 Map
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    private static final long EXPIRATION_TIME_MS = 5 * 60 * 1000; // 5분

    public void sendVerificationCode(String toEmail) {
        String code = generateCode();

        // 메모리에 코드와 만료시간 저장
        verificationCodes.put(toEmail, code);
        expirationTimes.put(toEmail, System.currentTimeMillis() + EXPIRATION_TIME_MS);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[모닥모닥] 이메일 인증 번호 안내");
        message.setText("안녕하세요.\n\n요청하신 인증 번호는 다음과 같습니다:\n"
                + code + "\n\n5분 이내에 입력해 주세요.");

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        Long expirationTime = expirationTimes.get(email);
        String savedCode = verificationCodes.get(email);

        if (expirationTime == null || savedCode == null) {
            return false;
        }

        if (System.currentTimeMillis() > expirationTime) {
            // 만료된 경우 삭제
            verificationCodes.remove(email);
            expirationTimes.remove(email);
            return false;
        }

        boolean isMatch = savedCode.equals(code);

        if (isMatch) {
            // 인증 성공시 삭제 처리 (1회용)
            verificationCodes.remove(email);
            expirationTimes.remove(email);
        }

        return isMatch;
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}

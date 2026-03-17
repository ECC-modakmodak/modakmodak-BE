package modak.modakmodak.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindIdVerifyRequest(
        @Schema(description = "가입 시 사용한 닉네임", example = "홍길동")
        String nickname,
        @Schema(description = "가입 시 사용한 이메일", example = "test@example.com")
        String email,
        @Schema(description = "이메일로 받은 인증번호", example = "123456")
        String code
) {}
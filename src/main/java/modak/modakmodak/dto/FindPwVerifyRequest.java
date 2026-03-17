package modak.modakmodak.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindPwVerifyRequest(
        @Schema(description = "가입 시 사용한 아이디", example = "hi1234")
        String username,
        @Schema(description = "가입 시 사용한 이메일", example = "test@example.com")
        String email,
        @Schema(description = "이메일로 받은 인증번호", example = "123456")
        String code
) {}
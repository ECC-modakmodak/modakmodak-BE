package modak.modakmodak.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindPwResetRequest(
        @Schema(description = "가입 시 사용한 아이디", example = "hi1234")
        String username,
        @Schema(description = "새로 설정할 비밀번호", example = "newPassword123!")
        String newPassword
) {}
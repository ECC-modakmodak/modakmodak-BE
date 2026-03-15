package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindPwRequest(
        @Schema(description = "아이디", example = "modak_test2")
        String username,
        @Schema(description = "이메일", example = "test@example.com")
        String email,
        @Schema(description = "이메일로 받은 인증번호", example = "123456")
        String code
) {}
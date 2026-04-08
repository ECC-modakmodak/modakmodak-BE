package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResetPwRequest(
        @Schema(description = "아이디", example = "modak_test2")
        String username,
        @Schema(description = "현재(또는 임시) 비밀번호", example = "temp1234!")
        String oldPassword,
        @Schema(description = "새로 바꿀 비밀번호", example = "new_password123")
        String newPassword
) {}
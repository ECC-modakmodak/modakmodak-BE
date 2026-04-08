package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WithdrawRequest(
        @Schema(description = "아이디", example = "modak_test2")
        String username,
        @Schema(description = "비밀번호 확인", example = "password123")
        String password
) {}
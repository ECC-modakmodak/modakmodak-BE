package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 정보")
public record UserLoginRequest(
        @Schema(description = "아이디", example = "modak_test2")
        String username,

        @Schema(description = "비밀번호", example = "password123")
        String password
) {}
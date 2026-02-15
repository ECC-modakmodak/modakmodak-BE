package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 데이터")
public record UserLoginResponse(
        @Schema(description = "사용자의 고유 ID (숫자)", example = "1")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "모닥이")
        String nickname,

        @Schema(description = "응답 메시지", example = "로그인 성공!")
        String message
) {}
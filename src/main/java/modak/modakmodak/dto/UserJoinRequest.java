package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청 정보")
public record UserJoinRequest(
        @Schema(description = "로그인 아이디", example = "jeein")
        String username,

        @Schema(description = "비밀번호", example = "pw123456")
        String password,

        @Schema(description = "이메일", example = "seo@example.com")
        String email,

        @Schema(description = "닉네임", example = "지인")
        String nickname
) {}
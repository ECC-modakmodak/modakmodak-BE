package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindIdRequest(
        @Schema(description = "가입 시 사용한 이메일", example = "test@example.com")
        String email
) {}
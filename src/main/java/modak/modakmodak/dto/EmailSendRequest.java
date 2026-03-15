package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailSendRequest(
        @Schema(description = "인증번호를 받을 이메일", example = "test@example.com")
        String email
) {}
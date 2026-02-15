package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import modak.modakmodak.entity.MeetingAtmosphere;

@Schema(description = "프로필 수정 요청 정보")
public record UserProfileRequest(
        @Schema(description = "새로운 닉네임", example = "이화나비")
        String nickname,

        @Schema(description = "이메일 주소", example = "jeein@ewha.ac.kr")
        String email,

        @Schema(description = "프로필 이미지 URL", example = "https://...")
        String profileImage,

        @Schema(description = "상태 메시지", example = "오늘도 화이팅!")
        String targetMessage,

        @Schema(description = "주 활동 지역", example = "서대문구")
        String activityArea
) {}
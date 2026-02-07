package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingMethod;

@Schema(description = "회원가입 요청 정보")
public record UserJoinRequest(
        @Schema(description = "로그인 아이디", example = "jeein")
        String username,

        @Schema(description = "비밀번호", example = "pw123456")
        String password,

        @Schema(description = "이메일", example = "seo@example.com")
        String email,

        @Schema(description = "닉네임", example = "지인")
        String nickname,

        @Schema(description = "선호 분위기", example = "도란도란")
                MeetingAtmosphere preferredType, // 조용히, 도란도란

        @Schema(description = "선호 방식", example = "대면")
        MeetingMethod preferredMethod,   // 대면, 비대면

        @Schema(description = "주요 활동 지역", example = "서울시 서대문구")
        String activityArea,             //

        @Schema(description = "나의 목표", example = "웹 개발 정복하기!")
        String targetMessage             //
) {}
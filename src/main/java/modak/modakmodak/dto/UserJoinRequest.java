package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingMethod;
import modak.modakmodak.entity.PreferredDay;
import modak.modakmodak.entity.PreferredTime;
import modak.modakmodak.entity.StudyCategory;

@Schema(description = "회원가입 요청 정보")
public record UserJoinRequest(
                @Schema(description = "로그인 아이디", example = "jeein") String username,

                @Schema(description = "비밀번호 (영문자, 숫자, 특수문자 포함 8-20자)", example = "pw123456!") String password,

                @Schema(description = "이메일", example = "seo@example.com") String email,

                @Schema(description = "닉네임", example = "지인") String nickname,

                @Schema(description = "선호 분위기", example = "CHATTY") MeetingAtmosphere preferredType, // 조용히, 도란도란

                @Schema(description = "선호 방식", example = "대면") MeetingMethod preferredMethod, // 대면, 비대면

        @Schema(description = "선호 요일", example = "WEEKDAY")
        PreferredDay preferredDay,       // 평일, 주말

        @Schema(description = "선호 시간대", example = "MORNING")
        PreferredTime preferredTime,     // 오전, 오후, 밤

        @Schema(description = "공부 분야", example = "PROGRAMMING")
        StudyCategory studyCategory,     // 파이썬, 어학 등

        @Schema(description = "주요 활동 지역", example = "서울시 서대문구")
        String activityArea,

                @Schema(description = "나의 목표", example = "웹 개발 정복하기!") String targetMessage //
) {
}
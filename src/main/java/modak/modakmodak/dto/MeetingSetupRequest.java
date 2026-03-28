package modak.modakmodak.dto;

import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;
import modak.modakmodak.entity.MeetingPodCategory;

public record MeetingSetupRequest(
        MeetingAtmosphere atmosphere, // 도란도란 vs 조용한
        MeetingCategory category,     // 카공 vs 줌공 vs 기타
        MeetingPodCategory podCategory, // 추가: 시험대비, 과제팀플 등
        String categoryEtc,           // "기타" 선택 시 사용자가 직접 쓴 글자
        int maxParticipants           // 최대 인원
) {}
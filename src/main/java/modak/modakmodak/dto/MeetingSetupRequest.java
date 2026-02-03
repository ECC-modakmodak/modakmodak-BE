package modak.modakmodak.dto;

import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;

public record MeetingSetupRequest(
        MeetingAtmosphere atmosphere, // 도란도란 vs 조용한
        MeetingCategory category,     // 카공 vs 줌공 vs 기타
        String categoryEtc,           // "기타" 선택 시 사용자가 직접 쓴 글자
        int maxParticipants           // 최대 인원
) {}
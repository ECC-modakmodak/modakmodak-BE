package modak.modakmodak.dto;

import java.util.List;

public record MeetingListResponse(
        int status,
        TodayMeetingDto todayData,
        List<MeetingDto> totalGroupData) {
}

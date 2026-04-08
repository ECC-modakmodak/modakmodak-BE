package modak.modakmodak.dto;

import java.util.List;

public record TodayMeetingDto(
        Long meetingId,
        String spot,
        String title,
        String groupTime,
        String goal,
        List<String> hashtags) {
}

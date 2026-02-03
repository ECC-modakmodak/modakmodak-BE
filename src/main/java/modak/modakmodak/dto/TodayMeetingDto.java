package modak.modakmodak.dto;

import java.util.List;

public record TodayMeetingDto(
        String spot,
        String title,
        String groupTime,
        List<String> hashtags) {
}

package modak.modakmodak.dto;

import java.util.List;

public record MeetingDto(
        Long meetingId,
        String title,
        String hostNickname,
        int currentParticipants,
        int maxParticipants,
        String date,
        List<String> hashtags) {
}

package modak.modakmodak.dto;

import java.util.List;

public record MeetingDetailResponse(
        int status,
        String message,
        MeetingData data
) {
    public record MeetingData(
            Long meetingId,
            String title,
            String description,
            String area,
            String locationDetail,
            String date,
            List<String> hashtags,
            String hostAnnouncement,
            ParticipantInfo participants,
            UserStatus userStatus
    ) {}

    public record ParticipantInfo(
            int current,
            int max,
            List<MemberDetail> list
    ) {}

    public record MemberDetail(
            Long memberId,
            String nickname,
            boolean isHost,
            String profileImage,
            String goal,
            boolean hasGoal,
            String reactionEmoji
    ) {}

    public record UserStatus(
            boolean isHost,
            String participationStatus
    ) {}
}
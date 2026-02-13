package modak.modakmodak.dto;

import java.util.List;

public record MeetingDetailResponse(
                int status,
                String message,
                MeetingData data) {
        public record MeetingData(
                        Long meetingId,
                        String title,
                        String createdAt, // 모임 생성 시간
                        String representativeImage,
                        String description,
                        String area,
                        String locationDetail,
                        String date, // 모임 약속 날짜
                        List<String> hashtags,
                        String hostAnnouncement,
                        ParticipantInfo participants,
                        UserStatus userStatus) {
        }

        public record ParticipantInfo(
                        int current,
                        int max,
                        List<MemberDetail> list) {
        }

        public record MemberDetail(
                        Long memberId,
                        String nickname,
                        boolean isHost,
                        String profileImage,
                        String targetMessage,
                        boolean hasGoal,
                        String reactionEmoji,
                        boolean attended ){
        }

        public record UserStatus(
                        boolean isHost,
                        String participationStatus) {
        }
}
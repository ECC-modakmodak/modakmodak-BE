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
                        Long hostId,
                        String hostNickname,
                        ParticipantInfo participants,
                        UserStatus userStatus) {
        }

        public record ParticipantInfo(
                        int current,
                        int max,
                        List<MemberDetail> list) {
        }

        public record MemberDetail(
                        Long participantId,
                        Long memberId,
                        String nickname,
                        String username,
                        boolean isHost,
                        String profileImage,
                        String targetMessage,
                        boolean hasGoal,
                        String displayedGoal,
                        String statusBadge,
                        boolean attended,
                        float attendanceRate, // [New] 출석률 추가
                        List<String> hashtags) {
        }

        public record UserStatus(
                        boolean isHost,
                        String participationStatus) {
        }
}
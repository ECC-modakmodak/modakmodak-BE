package modak.modakmodak.dto;

import java.util.List;

public record NotificationListResponse(
                List<NotificationDto> notifications) {
        public record NotificationDto(
                        Long notificationId,
                        String type,
                        String title,
                        String message,
                        Boolean isRead,
                        Long relatedId,
                        Long applicationId, // 참여 신청 ID (MEETING_APPLICATION 타입에서 사용)
                        String senderNickname,
                        String podName,
                        String profileImage,
                        String createdAt) {
        }
}

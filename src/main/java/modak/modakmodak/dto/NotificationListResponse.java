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
                        String senderNickname,
                        String podName,
                        String profileImage,
                        String createdAt) {
        }
}

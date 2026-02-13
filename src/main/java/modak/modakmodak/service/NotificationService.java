package modak.modakmodak.service;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.NotificationListResponse;
import modak.modakmodak.entity.Notification;
import modak.modakmodak.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotificationList(Long userId) {
        // 사용자의 알림 목록 조회 (최신순)
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // DTO로 변환
        List<NotificationListResponse.NotificationDto> notificationDtos = notifications.stream()
                .map(notification -> new NotificationListResponse.NotificationDto(
                        notification.getId(),
                        notification.getType().name(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getIsRead(),
                        notification.getRelatedId(),
                        notification.getCreatedAt().toString()))
                .toList();

        return new NotificationListResponse(notificationDtos);
    }

    @Transactional
    public modak.modakmodak.dto.NotificationReadResponse markNotificationAsRead(Long userId, Long notificationId) {
        // 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 본인의 알림인지 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림에 접근할 권한이 없습니다.");
        }

        // 읽음 처리
        notification.markAsRead();

        return new modak.modakmodak.dto.NotificationReadResponse("알림을 읽음 처리했습니다.");
    }
}

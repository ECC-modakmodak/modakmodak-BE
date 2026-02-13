package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.NotificationListResponse;
import modak.modakmodak.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "내 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotificationList(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        NotificationListResponse response = notificationService.getNotificationList(userId);
        return ResponseEntity.ok(response);
    }
}

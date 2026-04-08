package modak.modakmodak.repository;

import modak.modakmodak.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자의 알림 목록 조회 (최신순)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자의 미열람 알림 개수 조회
    int countByUserIdAndIsReadFalse(Long userId);
}

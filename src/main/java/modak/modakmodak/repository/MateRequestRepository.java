package modak.modakmodak.repository;

import modak.modakmodak.entity.MateRequest;
import modak.modakmodak.entity.MateRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateRequestRepository extends JpaRepository<MateRequest, Long> {

    // 중복 신청 확인 (from -> to)
    boolean existsByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    // 특정 상태의 요청 존재 확인
    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, MateRequestStatus status);

    // 받은 메이트 요청 목록 조회
    java.util.List<MateRequest> findByToUserId(Long toUserId);
}

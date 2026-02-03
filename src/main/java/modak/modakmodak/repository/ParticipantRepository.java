package modak.modakmodak.repository;

import modak.modakmodak.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    // 특정 모임에 참여 중인 사람들을 모두 가져오는 메서드
    List<Participant> findByMeetingId(Long meetingId);

    // 모임의 방장 찾기
    Participant findByMeetingIdAndIsHostTrue(Long meetingId);

    // 모임 참여 인원 수
    int countByMeetingId(Long meetingId);

    // 중복 신청 여부 확인
    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);
}
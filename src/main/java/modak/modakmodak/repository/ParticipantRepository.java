package modak.modakmodak.repository;

import modak.modakmodak.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    // 특정 모임에 참여 중인 사람들을 모두 가져오는 메서드
    List<Participant> findByMeetingId(Long meetingId);
}
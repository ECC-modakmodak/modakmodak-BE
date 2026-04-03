package modak.modakmodak.repository;

import modak.modakmodak.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByMeetingIdOrderByCreatedAtAsc(Long meetingId); // 시간 순으로 채팅 불러오기

    @Transactional
    void deleteAllByMeetingId(Long meetingId); // 팟 채팅 내역 일괄 삭제
}

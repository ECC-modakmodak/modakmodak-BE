package modak.modakmodak.service;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingStatusUpdateRequest;
import modak.modakmodak.dto.MeetingStatusUpdateResponse;
import modak.modakmodak.dto.ParticipantGoalRequest;
import modak.modakmodak.entity.Participant;
import modak.modakmodak.entity.ReactionEmoji;
import modak.modakmodak.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantService {
    private final ParticipantRepository participantRepository;

    // 리액션 이모지 수정 로직을 여기로 가져옵니다.
    public modak.modakmodak.dto.MeetingStatusUpdateResponse updateReactionEmoji(Long userId, Long participantId, MeetingStatusUpdateRequest request) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 정보를 찾을 수 없습니다."));

        if (!participant.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 상태만 수정할 수 있습니다.");
        }

        // Enum으로 변환하여 저장
        ReactionEmoji emoji = ReactionEmoji.valueOf(request.statusBadge());
        participant.setReactionEmoji(emoji);

        return new modak.modakmodak.dto.MeetingStatusUpdateResponse(
                200, "상태 업데이트 성공",
                new modak.modakmodak.dto.MeetingStatusUpdateResponse.StatusData(
                        participant.getId(), participant.getReactionEmoji().name())
        );
    }

    // 목표 수정 로직도 여기로 가져오면 더 좋아요!
    public void updateParticipantGoal(Long userId, Long participantId, ParticipantGoalRequest request) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 정보를 찾을 수 없습니다."));
        if (!participant.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 목표만 수정할 수 있습니다.");
        }
        participant.updateGoal(request.goal());
    }

    public void leaveMeeting(Long userId, Long meetingId) {
        // 1. 참여 정보 조회
        Participant participant = participantRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임에 참여하고 있지 않습니다."));

        // 2. 방장 여부 확인 (방장은 나갈 수 없음)
        if (participant.isHost()) {
            throw new IllegalStateException("방장은 모임에서 나갈 수 없습니다. 모임을 종료하거나 삭제해야 합니다.");
        }

        // 3. 시간 제한 확인 (현재 시간이 모임 시작 시간보다 이전이어야 함)
        LocalDateTime meetingTime = participant.getMeeting().getDate();
        if (meetingTime != null && LocalDateTime.now().isAfter(meetingTime)) {
            throw new IllegalStateException("이미 시작된 모임에서는 나갈 수 없습니다.");
        }

        // 4. 참여 정보 삭제 (모임 나가기)
        participantRepository.delete(participant);
    }
}
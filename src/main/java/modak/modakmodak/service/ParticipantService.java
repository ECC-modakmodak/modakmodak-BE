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
}
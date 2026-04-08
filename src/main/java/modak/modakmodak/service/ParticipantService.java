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
import modak.modakmodak.repository.MeetingRepository;
import modak.modakmodak.repository.UserRepository;
import java.util.Optional;
import modak.modakmodak.repository.MeetingRepository;
import modak.modakmodak.repository.ParticipantRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public void replyToPodInvite(Long userId, Long meetingId, modak.modakmodak.dto.PodInviteReplyRequest request) {
        // 1. 팟(Meeting)과 유저 정보 확인
        modak.modakmodak.entity.Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        modak.modakmodak.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("ACCEPTED".equals(request.status())) {
            // 이미 참여 중인지 중복 체크 (선택 사항이지만 권장)
            if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
                throw new IllegalStateException("이미 해당 모임에 참여 중입니다.");
            }

            // 수락했으므로 Participant 테이블에 APPROVED 상태로 완전히 추가!
            Participant participant = Participant.builder()
                    .meeting(meeting)
                    .user(user)
                    .status(modak.modakmodak.entity.ParticipationStatus.APPROVED) // 수락 시 바로 참여 완료
                    .isHost(false)
                    .attended(false)
                    .build();
            participantRepository.save(participant);

        } else if ("REJECTED".equals(request.status())) {
            // 거절한 경우 Participant 테이블에 넣지 않습니다.
            // 필요하다면 여기서 방장에게 "거절 사유(request.reason())"를 담아 거절 알림을 보낼 수도 있습니다.
            System.out.println("초대 거절됨. 사유: " + request.reason());
        } else {
            throw new IllegalArgumentException("잘못된 상태 값입니다. (ACCEPTED 또는 REJECTED 필요)");
        }
    }

    @Transactional
    public void removeParticipant(Long userId, Long meetingId, Long participantId) {
        // 1. 해당 모임이 존재하는지 확인
        modak.modakmodak.entity.Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

        // 2. 요청자가 방장(Host)인지 권한 검사
        if (!meeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("참가자 삭제 권한이 없습니다. 방장만 가능합니다.");
        }

        // 3. 삭제할 참가자가 해당 모임에 속해 있는지 확인
        modak.modakmodak.entity.Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참가 정보를 찾을 수 없습니다. ID: " + participantId));

        if (!participant.getMeeting().getId().equals(meetingId)) {
            throw new IllegalArgumentException("해당 모임에 속한 참가자가 아닙니다.");
        }

        // 4. 방장 본인은 삭제할 수 없음 (선택 사항)
        if (participant.isHost()) { // getIsHost() 대신 isHost()로 수정
            throw new IllegalArgumentException("방장 자신은 삭제할 수 없습니다.");
        }

        // 5. 삭제 실행
        participantRepository.delete(participant);
    }
}
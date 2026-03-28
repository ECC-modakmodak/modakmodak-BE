package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.ParticipantGoalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import modak.modakmodak.service.ParticipantService;
import java.util.Map;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {
    private final ParticipantService participantService;

    @Operation(summary = "리액션 이모지 수정", description = "참여자가 자신의 상태 이모지를 수정합니다.")
    @PatchMapping("/{participantId}/reaction-emoji")
    public ResponseEntity<modak.modakmodak.dto.MeetingStatusUpdateResponse> updateReactionEmoji(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long participantId,
            @RequestBody modak.modakmodak.dto.MeetingStatusUpdateRequest request) {

        return ResponseEntity.ok(participantService.updateReactionEmoji(userId, participantId, request));    }

    @Operation(summary = "팟 목표 수정", description = "참여자가 해당 모임에서 이룰 개인 목표를 수정합니다.")
    @PatchMapping("/{participantId}/goal")
    public ResponseEntity<String> updateParticipantGoal(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long participantId,
            @RequestBody ParticipantGoalRequest request) {

        participantService.updateParticipantGoal(userId, participantId, request);        return ResponseEntity.ok("팟 목표가 수정되었습니다.");
    }

    @Operation(summary = "모임 나가기", description = "일반 참여자가 모임 시작 전까지 모임에서 나갑니다. (방장 불가)")
    @DeleteMapping("/meetings/{meetingId}/leave")
    public ResponseEntity<Map<String, Object>> leaveMeeting(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long meetingId) {

        participantService.leaveMeeting(userId, meetingId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "모임에서 정상적으로 나갔습니다."
        ));
    }

    @Operation(summary = "팟 초대 수락/거절", description = "알림을 통해 받은 팟 초대를 수락하거나 거절합니다.")
    @PostMapping("/meetings/{meetingId}/invitations/reply")
    public ResponseEntity<Map<String, Object>> replyToPodInvite(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long meetingId,
            @RequestBody modak.modakmodak.dto.PodInviteReplyRequest request) {

        participantService.replyToPodInvite(userId, meetingId, request);

        String messageResult = "ACCEPTED".equals(request.status())
                ? "팟 초대를 수락하여 모임에 참여했습니다."
                : "팟 초대를 거절했습니다.";

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", messageResult
        ));
    }

    @Operation(summary = "참가자 삭제", description = "방장이 특정 참가자를 모임에서 삭제(강퇴)합니다.")
    @DeleteMapping("/{meetingId}/participants/{participantId}")
    public ResponseEntity<?> removeParticipant(
            @PathVariable Long meetingId,
            @PathVariable Long participantId,
            @RequestHeader("X-User-Id") Long userId) {

        // 방금 만든 서비스 로직 호출!
        participantService.removeParticipant(userId, meetingId, participantId);

        return ResponseEntity.ok("참가자가 성공적으로 삭제되었습니다.");
    }
}


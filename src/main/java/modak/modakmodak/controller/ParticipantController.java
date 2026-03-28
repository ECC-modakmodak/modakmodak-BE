package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.ParticipantGoalRequest;
import modak.modakmodak.meeting.MeetingService;
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
}


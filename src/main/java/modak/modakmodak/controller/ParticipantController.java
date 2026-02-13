package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.ParticipantGoalRequest;
import modak.modakmodak.meeting.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {
    private final MeetingService meetingService;

    @Operation(summary = "팟 목표 수정", description = "참여자가 해당 모임에서 이룰 개인 목표를 수정합니다.")
    @PatchMapping("/{participantId}/goal")
    public ResponseEntity<String> updateParticipantGoal(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long participantId,
            @RequestBody ParticipantGoalRequest request) {

        meetingService.updateParticipantGoal(userId, participantId, request);
        return ResponseEntity.ok("팟 목표가 수정되었습니다.");
    }
}
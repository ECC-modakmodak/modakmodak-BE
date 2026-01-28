package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.meeting.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Meeting", description = "모임 개설 API")
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @Operation(summary = "모임 초기 성격 설정", description = "1단계: 분위기, 카테고리, 인원을 설정합니다.")
    @PostMapping("/setup")
    public ResponseEntity<Long> setup(@RequestBody MeetingSetupRequest request) {
        return ResponseEntity.ok(meetingService.setupMeeting(request));
    }

    @Operation(summary = "모임 세부 설정 및 개설", description = "2단계: 상세 정보를 입력하여 모임을 개설합니다.")
    @PostMapping("/{meetingId}/details")
    public ResponseEntity<String> complete(
            @PathVariable Long meetingId,
            @RequestBody MeetingDetailRequest request) {
        meetingService.completeMeeting(meetingId, request);
        return ResponseEntity.ok("모임 개설이 완료되었습니다!");
    }
}
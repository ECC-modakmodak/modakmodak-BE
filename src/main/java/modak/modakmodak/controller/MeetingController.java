package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.dto.MeetingDetailResponse;
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

    @Operation(summary = "모임 상세 조회", description = "특정 모임의 상세 정보와 참여자 목록을 조회합니다.")
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(@PathVariable Long meetingId) {
        // 1. 서비스의 getMeetingDetail 메서드를 호출해서 DB의 진짜 데이터를 받아옵니다.
        MeetingDetailResponse.MeetingData data = meetingService.getMeetingDetail(meetingId);

        // 2. null 대신 실제 data를 담아서 보냅니다.
        return ResponseEntity.ok(new MeetingDetailResponse(200, "모임 상세 조회 성공", data));
    }
}
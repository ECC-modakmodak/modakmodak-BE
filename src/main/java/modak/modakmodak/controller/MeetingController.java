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
import modak.modakmodak.dto.MeetingUpdateDetailRequest;

@Tag(name = "Meeting", description = "모임 개설 API")
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @Operation(summary = "모임 초기 성격 설정", description = "1단계: 분위기, 카테고리, 인원을 설정합니다.")
    @PostMapping("/setup")
    public ResponseEntity<Long> setup(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody MeetingSetupRequest request) {
        return ResponseEntity.ok(meetingService.setupMeeting(userId, request));
    }

    @Operation(summary = "모임 세부 설정 및 개설", description = "2단계: 상세 정보를 입력하여 모임을 개설합니다.")
    @PostMapping("/{meetingId}/details")
    public ResponseEntity<String> complete(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @RequestBody MeetingDetailRequest request) {
        meetingService.completeMeeting(userId, meetingId, request);
        return ResponseEntity.ok("모임 개설이 완료되었습니다!");
    }

    @Operation(summary = "모임 상세 조회", description = "특정 모임의 상세 정보와 참여자 목록을 조회합니다.")
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId) {
        // 1. 서비스의 getMeetingDetail 메서드를 호출해서 DB의 진짜 데이터를 받아옵니다.
        MeetingDetailResponse.MeetingData data = meetingService.getMeetingDetail(userId, meetingId);

        // 2. null 대신 실제 data를 담아서 보냅니다.
        return ResponseEntity.ok(new MeetingDetailResponse(200, "모임 상세 조회 성공", data));
    }

    @Operation(summary = "모임 목록 조회", description = "메인 화면에서 모임 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<modak.modakmodak.dto.MeetingListResponse> getMeetingList() {
        return ResponseEntity.ok(meetingService.getMeetingList());
    }

    @Operation(summary = "모임 참여 신청", description = "특정 모임에 참여를 신청합니다.")
    @PostMapping("/{meetingId}/apply")
    public ResponseEntity<modak.modakmodak.dto.MeetingApplicationResponse> applyMeeting(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @RequestBody modak.modakmodak.dto.MeetingApplicationRequest request) {
        return ResponseEntity.ok(meetingService.applyMeeting(userId, meetingId, request));
    }

    @Operation(summary = "모임 참여 승인/거절", description = "방장이 신청자의 참여 요청을 승인하거나 거절합니다.")
    @PatchMapping("/{meetingId}/approve/{applicationId}")
    public ResponseEntity<modak.modakmodak.dto.MeetingApprovalResponse> approveApplication(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @PathVariable Long applicationId,
            @RequestBody modak.modakmodak.dto.MeetingApprovalRequest request) {
        return ResponseEntity.ok(meetingService.approveApplication(userId, meetingId, applicationId, request));
    }

    @Operation(summary = "팟 종료", description = "방장이 팟을 종료합니다.")
    @PatchMapping("/{meetingId}/complete")
    public ResponseEntity<modak.modakmodak.dto.MeetingCompleteResponse> completeMeeting(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.completeMeetingByHost(userId, meetingId));
    }

    @Operation(summary = "출석 체크", description = "팟장이 참여자의 출석 여부를 체크합니다.")
    @PatchMapping("/{meetingId}/attendance")
    public ResponseEntity<modak.modakmodak.dto.AttendanceCheckResponse> checkAttendance(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @RequestBody modak.modakmodak.dto.AttendanceCheckRequest request) {
        return ResponseEntity.ok(meetingService.checkAttendance(userId, meetingId, request));
    }

    @Operation(summary = "모임 상세 정보 부분 수정", description = "방장이 지역, 장소, 공지사항을 선택적으로 수정합니다.")
    @PatchMapping("/{meetingId}/details/update") // 기존 complete와 경로 중복 방지를 위해 수정
    public ResponseEntity<String> updateMeetingDetail(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @RequestBody MeetingUpdateDetailRequest request) {
        meetingService.updateMeetingDetail(userId, meetingId, request);
        return ResponseEntity.ok("모임 정보가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "참가자 삭제", description = "팟장이 참가자를 삭제합니다.")
    @DeleteMapping("/{meetingId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long meetingId,
            @PathVariable Long participantId) {
        meetingService.removeParticipant(userId, meetingId, participantId);
        return ResponseEntity.ok().build();
    }
}
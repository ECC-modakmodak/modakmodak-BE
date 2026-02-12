package modak.modakmodak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MateRequestRequest;
import modak.modakmodak.dto.MateRequestResponse;
import modak.modakmodak.service.MateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mate", description = "메이트 관련 API")
@RestController
@RequestMapping("/api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;

    @Operation(summary = "메이트 신청", description = "다른 사용자에게 메이트 신청을 보냅니다.")
    @PostMapping("/requests")
    public ResponseEntity<MateRequestResponse> sendMateRequest(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody MateRequestRequest request) {
        MateRequestResponse response = mateService.sendMateRequest(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "메이트 요청 목록 조회", description = "내가 받은 메이트 요청 목록을 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<modak.modakmodak.dto.MateRequestListResponse> getMateRequestList(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        modak.modakmodak.dto.MateRequestListResponse response = mateService.getMateRequestList(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메이트 요청 승인/거절", description = "받은 메이트 요청을 승인하거나 거절합니다.")
    @PatchMapping("/requests/{requestId}")
    public ResponseEntity<modak.modakmodak.dto.MateApprovalResponse> approveMateRequest(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long requestId,
            @RequestBody modak.modakmodak.dto.MateApprovalRequest request) {
        modak.modakmodak.dto.MateApprovalResponse response = mateService.approveMateRequest(userId, requestId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 메이트 목록 조회", description = "내 메이트 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<modak.modakmodak.dto.MateListResponse> getMateList(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        modak.modakmodak.dto.MateListResponse response = mateService.getMateList(userId);
        return ResponseEntity.ok(response);
    }
}

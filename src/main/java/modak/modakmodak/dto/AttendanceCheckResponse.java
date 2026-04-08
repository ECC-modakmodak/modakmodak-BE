package modak.modakmodak.dto;

import lombok.Builder;

@Builder
public record AttendanceCheckResponse(
        int status,
        String message,
        AttendanceData data) {
    @Builder
    public record AttendanceData(
            Long participantId,
            Long userId,
            String nickname,
            Boolean attended) {
    }
}

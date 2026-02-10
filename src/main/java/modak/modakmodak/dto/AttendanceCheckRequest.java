package modak.modakmodak.dto;

import lombok.Builder;

@Builder
public record AttendanceCheckRequest(
        Long participantId,
        Boolean attended) {
}

package modak.modakmodak.dto;

public record MeetingStatusUpdateResponse(
        int status,
        String message,
        StatusData data) {
    public record StatusData(
            Long memberId,
            String statusBadge) {
    }
}

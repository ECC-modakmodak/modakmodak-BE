package modak.modakmodak.dto;

public record MeetingStatusUpdateRequest(
        String statusBadge // e.g. "집중하고 있어요", "도착했어요"
) {
}

package modak.modakmodak.dto;

public record MeetingApplicationResponse(
        int status,
        String message,
        ApplicationData data) {
    public record ApplicationData(
            Long applicationId,
            String status) {
    }
}

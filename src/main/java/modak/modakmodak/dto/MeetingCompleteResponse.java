package modak.modakmodak.dto;

public record MeetingCompleteResponse(
        int status,
        String message,
        CompleteData data) {

    public record CompleteData(
            Long meetingId,
            Boolean isCompleted) {
    }
}

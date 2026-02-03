package modak.modakmodak.dto;

public record MeetingApprovalResponse(
        int status,
        String message,
        ApprovalData data) {
    public record ApprovalData(
            Long applicationId,
            String memberNickname,
            String updatedStatus,
            int currentParticipants) {
    }
}

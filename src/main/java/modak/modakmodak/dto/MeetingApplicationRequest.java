package modak.modakmodak.dto;

public record MeetingApplicationRequest(
        boolean agreedToRules,
        boolean agreedToNoShow,
        boolean agreedToPrivacy) {
}

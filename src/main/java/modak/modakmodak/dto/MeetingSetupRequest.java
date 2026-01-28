package modak.modakmodak.dto;

public record MeetingSetupRequest(
        String atmosphere,
        String category,
        int maxParticipants
) {}
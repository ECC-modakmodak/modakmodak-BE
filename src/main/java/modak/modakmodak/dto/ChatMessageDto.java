package modak.modakmodak.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
    Long meetingId,
    Long senderId,
    String senderNickname,
    boolean isHost,
    String message,
    LocalDateTime createAt
) {}

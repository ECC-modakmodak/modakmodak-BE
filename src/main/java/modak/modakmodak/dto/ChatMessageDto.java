package modak.modakmodak.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record ChatMessageDto(
    Long meetingId,
    Long senderId,
    String senderNickname,
    String senderProfileImageUrl,
    boolean isHost,
    String message,

    // 웹소켓 전송 시 JSON 직렬화 문제 방지
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime createdAt
) {}

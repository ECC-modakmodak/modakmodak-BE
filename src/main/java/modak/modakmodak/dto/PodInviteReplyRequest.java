package modak.modakmodak.dto;

public record PodInviteReplyRequest(
        String status, // "ACCEPTED" 또는 "REJECTED"
        String reason  // 거절 시 사유 (수락 시 null)
) {}
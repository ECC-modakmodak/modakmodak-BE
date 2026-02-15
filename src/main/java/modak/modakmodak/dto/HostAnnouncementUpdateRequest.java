package modak.modakmodak.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 공지사항 수정 요청")
public record HostAnnouncementUpdateRequest(
        @Schema(description = "새로운 공지사항 내용", example = "이번 모임은 7시까지 모여주세요!")
        String hostAnnouncement
) {}
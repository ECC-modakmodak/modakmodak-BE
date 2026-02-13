package modak.modakmodak.dto;

public record MeetingDetailRequest(
                String title,
                String date, // 예: "2026-01-28T15:00:00"
                String area,
                String locationDetail,
                String description,
                String imageUrl,
                String goal, // 팟 목표
                String hostAnnouncement
) {
}
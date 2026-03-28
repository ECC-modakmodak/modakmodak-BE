package modak.modakmodak.dto;

import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;
import modak.modakmodak.entity.MeetingPodCategory;

public record MeetingDetailRequest(
                String title,
                String date, // 예: "2026-01-28T15:00:00"
                String area,
                String locationDetail,
                String description,
                String imageUrl,
                String hostAnnouncement,
                modak.modakmodak.entity.MeetingAtmosphere atmosphere,
                modak.modakmodak.entity.MeetingCategory category,
                MeetingPodCategory podCategory,
                String categoryEtc,
                int maxParticipants
) {
}
package modak.modakmodak.dto;

import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;

public record MeetingUpdateDetailRequest(
        String title,
        String date,
        String area,
        String locationDetail,
        String description,
        String imageUrl,
        String hostAnnouncement,
        MeetingAtmosphere atmosphere,
        MeetingCategory category,
        String categoryEtc,
        int maxParticipants
) {}
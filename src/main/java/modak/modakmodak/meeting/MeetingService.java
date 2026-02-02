package modak.modakmodak.meeting;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.entity.Meeting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public Long setupMeeting(MeetingSetupRequest request) {
        Meeting meeting = Meeting.builder()
                .atmosphere(request.atmosphere())
                .category(request.category())
                .maxParticipants(request.maxParticipants())
                .status("PENDING")
                .build();
        return meetingRepository.save(meeting).getId();
    }

    public void completeMeeting(Long meetingId, MeetingDetailRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));
        meeting.updateDetails(request);
    }
}
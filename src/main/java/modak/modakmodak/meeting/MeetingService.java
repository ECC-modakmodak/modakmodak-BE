package modak.modakmodak.meeting;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.dto.MeetingDetailResponse;
import modak.modakmodak.entity.Meeting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final modak.modakmodak.repository.ParticipantRepository participantRepository;

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

    @Transactional(readOnly = true)
    public MeetingDetailResponse.MeetingData getMeetingDetail(Long meetingId) {
        // 1. DB에서 ID로 해당 모임을 찾습니다.
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

        // 2. DB에서 꺼낸 진짜 데이터(meeting.get...)들로 응답 객체를 만듭니다.
        return new MeetingDetailResponse.MeetingData(
                meeting.getId(),
                meeting.getTitle(), // DB의 진짜 제목
                meeting.getDescription(), // DB의 진짜 설명
                meeting.getArea(), // DB의 진짜 지역
                meeting.getLocationDetail(), // DB의 진짜 장소
                meeting.getDate() != null ? meeting.getDate().toString() : null, // 날짜 형식 변환
                List.of(meeting.getAtmosphere(), meeting.getCategory()), // 생성 시 넣은 성격/카테고리를 해시태그처럼 사용
                "방장이 등록한 공지사항이 이곳에 표시됩니다.", // 아직 DB 필드가 없다면 우선 가짜 텍스트
                null, // 참여자 목록 (추후 조인 조회로 구현)
                null // 내 상태 정보 (추후 세션/토큰 정보로 구현)
        );
    }

    @Transactional(readOnly = true)
    public modak.modakmodak.dto.MeetingListResponse getMeetingList() {
        List<Meeting> meetings = meetingRepository.findAll();

        List<modak.modakmodak.dto.MeetingDto> meetingDtos = meetings.stream().map(meeting -> {
            modak.modakmodak.entity.Participant host = participantRepository
                    .findByMeetingIdAndIsHostTrue(meeting.getId());
            String hostNickname = (host != null && host.getUser() != null) ? host.getUser().getNickname() : "알수없음";
            int count = participantRepository.countByMeetingId(meeting.getId());

            return new modak.modakmodak.dto.MeetingDto(
                    meeting.getId(),
                    meeting.getTitle(),
                    hostNickname,
                    count,
                    meeting.getMaxParticipants(),
                    meeting.getDate() != null ? meeting.getDate().toString() : "",
                    List.of(meeting.getAtmosphere(), meeting.getCategory()));
        }).toList();

        // 오늘의 팟 (임시로 첫 번째 모임 사용, 없으면 null)
        modak.modakmodak.dto.TodayMeetingDto todayData = null;
        if (!meetingDtos.isEmpty()) {
            modak.modakmodak.dto.MeetingDto first = meetingDtos.get(0);
            todayData = new modak.modakmodak.dto.TodayMeetingDto(
                    "이화여대", // spot 정보가 Meeting에 없어서 임시 고정값 사용
                    first.title(),
                    first.date(),
                    first.hashtags());
        }

        return new modak.modakmodak.dto.MeetingListResponse(
                200,
                todayData,
                meetingDtos);
    }
}
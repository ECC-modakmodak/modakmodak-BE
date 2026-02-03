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
        private final modak.modakmodak.repository.UserRepository userRepository;

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
                        String hostNickname = (host != null && host.getUser() != null) ? host.getUser().getNickname()
                                        : "알수없음";
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

        @Transactional
        public modak.modakmodak.dto.MeetingApplicationResponse applyMeeting(Long meetingId,
                        modak.modakmodak.dto.MeetingApplicationRequest request) {
                // 1. 모임 존재 확인
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 2. 임시 유저 (ID: 1) 조회 -> 실제로는 @AuthenticationPrincipal 등으로 받아야 함
                Long userId = 1L;
                modak.modakmodak.entity.User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

                // 3. 중복 신청 확인
                if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
                        throw new IllegalArgumentException("이미 해당 모임에 신청한 이력이 있습니다.");
                }

                // 4. 정원 초과 확인
                int currentCount = participantRepository.countByMeetingId(meetingId);
                if (currentCount >= meeting.getMaxParticipants()) {
                        throw new IllegalArgumentException("이미 정원이 찬 모임입니다.");
                }

                // 5. 참여 정보 저장 (검증된 상태라면 승인 대기가 아니라 바로 승인일 수도 있지만, 명세에 PENDING 이라 되어 있으므로
                // PENDING)
                // -> 명세서 Response 데이터에 "status": "PENDING"
                modak.modakmodak.entity.Participant participant = modak.modakmodak.entity.Participant.builder()
                                .meeting(meeting)
                                .user(user)
                                .status(modak.modakmodak.entity.ParticipationStatus.PENDING)
                                .isHost(false)
                                .build();

                participantRepository.save(participant);

                return new modak.modakmodak.dto.MeetingApplicationResponse(
                                201,
                                "참여 신청이 완료되었습니다.",
                                new modak.modakmodak.dto.MeetingApplicationResponse.ApplicationData(
                                                participant.getId(),
                                                participant.getStatus().name()));
        }

        @Transactional
        public modak.modakmodak.dto.MeetingApprovalResponse approveApplication(Long meetingId, Long applicationId,
                        modak.modakmodak.dto.MeetingApprovalRequest request) {
                // 1. 신청서 조회
                modak.modakmodak.entity.Participant participant = participantRepository.findById(applicationId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "존재하지 않는 신청서입니다. ID: " + applicationId));

                // 2. 모임 ID 검증
                if (!participant.getMeeting().getId().equals(meetingId)) {
                        throw new IllegalArgumentException("해당 모임의 신청서가 아닙니다.");
                }

                // 3. 상태 업데이트
                modak.modakmodak.entity.ParticipationStatus newStatus = modak.modakmodak.entity.ParticipationStatus
                                .valueOf(request.status());
                participant.updateStatus(newStatus);

                // 4. APPROVED 인 경우 정원 체크 (신청 시에도 체크하지만, 동시성 문제 등 대비)
                if (newStatus == modak.modakmodak.entity.ParticipationStatus.APPROVED) {
                        int currentCount = participantRepository.countByMeetingId(meetingId);
                        if (currentCount > participant.getMeeting().getMaxParticipants()) {
                                // 이미 정원 초과라면 다시 롤백하거나 예외 발생
                                throw new IllegalArgumentException("정원이 초과되어 승인할 수 없습니다.");
                        }
                }

                // 5. 변경 사항 저장 (Dirty Checking으로 자동 저장되지만 명시적으로 호출해도 무방)
                participantRepository.save(participant);

                // 6. 현재 인원 다시 계산
                int updatedCount = participantRepository.countByMeetingId(meetingId);
                String nickname = (participant.getUser() != null) ? participant.getUser().getNickname() : "알수없음";

                return new modak.modakmodak.dto.MeetingApprovalResponse(
                                200,
                                "신청자 승인 처리가 완료되었습니다.", // 거절일 경우 메시지 처리가 명세에 명확치 않으나, 일단 통일하거나 로직 분기 가능
                                new modak.modakmodak.dto.MeetingApprovalResponse.ApprovalData(
                                                participant.getId(),
                                                nickname,
                                                participant.getStatus().name(),
                                                updatedCount));
        }

        @Transactional
        public modak.modakmodak.dto.MeetingStatusUpdateResponse updateMeetingStatus(Long meetingId,
                        modak.modakmodak.dto.MeetingStatusUpdateRequest request) {
                // 1. 임시 유저 (ID: 1)
                Long userId = 1L;

                // 2. 해당 모임의 내 참여 정보 조회
                modak.modakmodak.entity.Participant participant = participantRepository.findByMeetingId(meetingId)
                                .stream()
                                .filter(p -> p.getUser().getId().equals(userId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("해당 모임에 참여하고 있지 않습니다."));

                // 3. 상태 배지 업데이트
                participant.updateStatusBadge(request.statusBadge());
                // participantRepository.save(participant); // Dirty Checking

                return new modak.modakmodak.dto.MeetingStatusUpdateResponse(
                                200,
                                "상태 업데이트 성공",
                                new modak.modakmodak.dto.MeetingStatusUpdateResponse.StatusData(
                                                participant.getId(),
                                                participant.getStatusBadge()));
        }
}
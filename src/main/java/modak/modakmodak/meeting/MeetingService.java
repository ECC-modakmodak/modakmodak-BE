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

        public Long setupMeeting(Long userId, MeetingSetupRequest request) {
                modak.modakmodak.entity.User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

                Meeting meeting = Meeting.builder()
                                .user(user)
                                .atmosphere(request.atmosphere()) // Enum으로 바로 저장
                                .category(request.category()) // Enum으로 바로 저장
                                .categoryEtc(request.categoryEtc()) // "기타" 내용 저장
                                .maxParticipants(request.maxParticipants())
                                .status("PENDING")
                                .build();
                Meeting savedMeeting = meetingRepository.save(meeting);

                // 방장(개설자) 참여 정보 저장 (자동 승인, 호스트 권한)
                modak.modakmodak.entity.Participant host = modak.modakmodak.entity.Participant.builder()
                                .meeting(savedMeeting)
                                .user(user)
                                .status(modak.modakmodak.entity.ParticipationStatus.APPROVED)
                                .isHost(true)
                                .build();
                participantRepository.save(host);

                return savedMeeting.getId();
        }

        public void completeMeeting(Long userId, Long meetingId, MeetingDetailRequest request) {
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 방장 검증
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("모임 설정 권한이 없습니다.");
                }

                meeting.updateDetails(request);
        }

        @Transactional(readOnly = true)
        public MeetingDetailResponse.MeetingData getMeetingDetail(Long userId, Long meetingId) {
                // 1. DB에서 ID로 해당 모임을 찾습니다.
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 2. DB에서 꺼낸 진짜 데이터(meeting.get...)들로 응답 객체를 만듭니다.
                return new MeetingDetailResponse.MeetingData(
                                meeting.getId(),
                                meeting.getTitle(), // DB의 진짜 제목
                                meeting.getCreatedAt() != null ? meeting.getCreatedAt().toString() : "",
                                "https://modak-bucket.s3.amazonaws.com/default-meeting.png",
                                meeting.getDescription(), // DB의 진짜 설명
                                meeting.getArea(), // DB의 진짜 지역
                                meeting.getLocationDetail(), // DB의 진짜 장소
                                meeting.getDate() != null ? meeting.getDate().toString() : null, // 날짜 형식 변환
                                List.of(
                                                meeting.getAtmosphere() != null ? meeting.getAtmosphere().name() : "기타",
                                                meeting.getCategory() != null ? meeting.getCategory().name() : "미정"),
                                "방장이 등록한 공지사항이 이곳에 표시됩니다.", // hostAnnouncement
                                null, // 참여자 목록 (추후 조인 조회로 구현)
                                null // 내 상태 정보 (추후 조회 구현)
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

                        // location 필드: area만 사용
                        String location = meeting.getArea() != null ? meeting.getArea() : "";

                        return new modak.modakmodak.dto.MeetingDto(
                                        meeting.getId(),
                                        meeting.getTitle(),
                                        meeting.getCreatedAt() != null ? meeting.getCreatedAt().toString() : "",
                                        "https://modak-bucket.s3.amazonaws.com/default-meeting.png",
                                        hostNickname,
                                        count,
                                        meeting.getMaxParticipants(),
                                        location,
                                        List.of(
                                                        meeting.getAtmosphere() != null ? meeting.getAtmosphere().name()
                                                                        : "기타",
                                                        meeting.getCategory() != null ? meeting.getCategory().name()
                                                                        : "미정"));
                }).toList();

                // 오늘의 팟: isCompleted가 false인 미팅 중 첫 번째 (종료되지 않은 팟만)
                modak.modakmodak.dto.TodayMeetingDto todayData = null;
                java.util.Optional<Meeting> activeMeeting = meetings.stream()
                                .filter(m -> m.getIsCompleted() != null && !m.getIsCompleted())
                                .findFirst();

                if (activeMeeting.isPresent()) {
                        Meeting meeting = activeMeeting.get();
                        todayData = new modak.modakmodak.dto.TodayMeetingDto(
                                        meeting.getId(),
                                        meeting.getArea() != null ? meeting.getArea() : "미정", // spot
                                        meeting.getTitle(),
                                        meeting.getDate() != null ? meeting.getDate().toString() : "",
                                        List.of(
                                                        meeting.getAtmosphere() != null ? meeting.getAtmosphere().name()
                                                                        : "기타",
                                                        meeting.getCategory() != null ? meeting.getCategory().name()
                                                                        : "미정"));
                }

                return new modak.modakmodak.dto.MeetingListResponse(
                                200,
                                todayData,
                                meetingDtos);
        }

        @Transactional
        public modak.modakmodak.dto.MeetingApplicationResponse applyMeeting(Long userId, Long meetingId,
                        modak.modakmodak.dto.MeetingApplicationRequest request) {
                // 1. 모임 존재 확인
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 2. 유저 조회
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

                // 5. 참여 정보 저장
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
        public modak.modakmodak.dto.MeetingApprovalResponse approveApplication(Long userId, Long meetingId,
                        Long applicationId,
                        modak.modakmodak.dto.MeetingApprovalRequest request) {

                // 0. 요청자가 해당 모임의 방장인지 확인
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("승인 권한이 없습니다 (방장이 아닙니다).");
                }

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
        public modak.modakmodak.dto.MeetingStatusUpdateResponse updateMeetingStatus(Long userId, Long meetingId,
                        modak.modakmodak.dto.MeetingStatusUpdateRequest request) {

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

        @Transactional
        public modak.modakmodak.dto.MeetingCompleteResponse completeMeetingByHost(Long userId, Long meetingId) {
                // 1. 모임 존재 확인
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 2. 방장 권한 확인
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("팟 종료 권한이 없습니다 (방장이 아닙니다).");
                }

                // 3. 이미 종료된 팟인지 확인
                if (meeting.getIsCompleted() != null && meeting.getIsCompleted()) {
                        throw new IllegalArgumentException("이미 종료된 팟입니다.");
                }

                // 4. 팟 종료 처리
                meeting.completeMeeting();

                return new modak.modakmodak.dto.MeetingCompleteResponse(
                                200,
                                "팟이 종료되었습니다.",
                                new modak.modakmodak.dto.MeetingCompleteResponse.CompleteData(
                                                meeting.getId(),
                                                meeting.getIsCompleted()));
        public modak.modakmodak.dto.AttendanceCheckResponse checkAttendance(Long userId, Long meetingId,
                        modak.modakmodak.dto.AttendanceCheckRequest request) {

                // 1. 요청자가 해당 모임의 팟장인지 확인
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("출석 체크 권한이 없습니다 (팟장이 아닙니다).");
                }

                // 2. 참여자 조회
                modak.modakmodak.entity.Participant participant = participantRepository
                                .findById(request.participantId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "존재하지 않는 참여자입니다. ID: " + request.participantId()));

                // 3. 해당 참여자가 이 모임의 참여자인지 확인
                if (!participant.getMeeting().getId().equals(meetingId)) {
                        throw new IllegalArgumentException("해당 모임의 참여자가 아닙니다.");
                }

                // 4. 출석 상태 업데이트
                participant.updateAttendance(request.attended());

                // 5. 응답 반환
                String nickname = (participant.getUser() != null) ? participant.getUser().getNickname() : "알수없음";
                Long participantUserId = (participant.getUser() != null) ? participant.getUser().getId() : null;

                return new modak.modakmodak.dto.AttendanceCheckResponse(
                                200,
                                "출석 체크가 완료되었습니다.",
                                new modak.modakmodak.dto.AttendanceCheckResponse.AttendanceData(
                                                participant.getId(),
                                                participantUserId,
                                                nickname,
                                                participant.getAttended()));
        }
}
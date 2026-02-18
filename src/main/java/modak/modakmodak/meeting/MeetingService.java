package modak.modakmodak.meeting;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.dto.MeetingDetailResponse;
import modak.modakmodak.entity.Meeting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import modak.modakmodak.entity.User;
import modak.modakmodak.entity.Participant;
import modak.modakmodak.entity.ParticipationStatus;
import java.util.stream.Collectors;
import modak.modakmodak.dto.MeetingUpdateDetailRequest;
import modak.modakmodak.dto.ParticipantGoalRequest;
import java.util.List;
import java.util.ArrayList;
import modak.modakmodak.dto.HostAnnouncementUpdateRequest;
import modak.modakmodak.dto.DateUpdateRequest;
import modak.modakmodak.dto.LocationDetailUpdateRequest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {
        private final MeetingRepository meetingRepository;
        private final modak.modakmodak.repository.ParticipantRepository participantRepository;
        private final modak.modakmodak.repository.UserRepository userRepository;
        private final modak.modakmodak.repository.NotificationRepository notificationRepository;

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

                // [Safety Fix] Capture existing values to ensure they are not lost
                modak.modakmodak.entity.MeetingAtmosphere oldAtmosphere = meeting.getAtmosphere();
                modak.modakmodak.entity.MeetingCategory oldCategory = meeting.getCategory();
                String oldCategoryEtc = meeting.getCategoryEtc();
                int oldMaxParticipants = meeting.getMaxParticipants();

                meeting.updateDetails(request);

                // [Safety Fix] If updated values are missing/invalid but old values existed,
                // restore them
                if (meeting.getAtmosphere() == null && oldAtmosphere != null) {
                        meeting.setAtmosphere(oldAtmosphere);
                }
                if (meeting.getCategory() == null && oldCategory != null) {
                        meeting.setCategory(oldCategory);
                }
                if (meeting.getCategoryEtc() == null && oldCategoryEtc != null) {
                        meeting.setCategoryEtc(oldCategoryEtc);
                }
                if (meeting.getMaxParticipants() <= 0 && oldMaxParticipants > 0) {
                        meeting.setMaxParticipants(oldMaxParticipants);
                }
        }

        @Transactional(readOnly = true)
        public MeetingDetailResponse.MeetingData getMeetingDetail(Long userId, Long meetingId) {
                // 1. DB에서 ID로 해당 모임을 찾습니다.
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. ID: " + meetingId));

                // 2. 해당 모임의 모든 참여자 정보 가져오기
                List<Participant> participants = participantRepository.findByMeetingId(meetingId);

                Long realHostId = meeting.getUser().getId();

                // 3. 참여자 목록 변환 (출석 여부 p.getAttended() 포함)
                List<MeetingDetailResponse.MemberDetail> memberDetails = participants.stream()
                                .filter(p -> p.getStatus() == ParticipationStatus.APPROVED) // APPROVED만 필터링
                                .map(p -> {
                                        User user = p.getUser();

                                        String displayedGoal = (p.getGoal() != null && !p.getGoal().isBlank())
                                                        ? p.getGoal()
                                                        : null;

                                        List<String> hashtags = new ArrayList<>();
                                        if (user.getPreferredType() != null)
                                                hashtags.add(user.getPreferredType().name());
                                        if (user.getPreferredMethod() != null)
                                                hashtags.add(user.getPreferredMethod().name());

                                        return new MeetingDetailResponse.MemberDetail(
                                                        p.getId(), // participantId
                                                        user.getId(), // memberId
                                                        user.getNickname(),
                                                        user.getUsername(),
                                                        user.getId().equals(realHostId), user.getProfileImage(),
                                                        user.getTargetMessage() != null ? user.getTargetMessage()
                                                                        : "기본 목표가 없습니다.", // 회원가입 시 적은 목표
                                                        p.getGoal() != null,
                                                        displayedGoal,
                                                        p.getReactionEmoji() != null ? p.getReactionEmoji().name()
                                                                        : null,
                                                        p.getAttended() != null ? p.getAttended() : false,
                                                        user.getAttendanceRate(), // [New] 출석률 추가
                                                        hashtags);
                                }).collect(Collectors.toList());

                // 4. 현재 조회 중인 유저의 상태 찾기
                Participant myStatus = participants.stream()
                                .filter(p -> p.getUser().getId().equals(userId))
                                .findFirst()
                                .orElse(null);

                // 5. [수정] 진짜 데이터들을 응답 객체에 담습니다.
                return new MeetingDetailResponse.MeetingData(
                                meeting.getId(),
                                meeting.getTitle(),
                                meeting.getCreatedAt() != null ? meeting.getCreatedAt().toString() : "",

                                // ◀ [중요] 고정 주소 대신 DB에 저장된 진짜 이미지 주소를 보냅니다.
                                meeting.getImageUrl(),

                                meeting.getDescription(),
                                meeting.getArea(),
                                meeting.getLocationDetail(),
                                meeting.getDate() != null ? meeting.getDate().toString() : null,
                                List.of(
                                                meeting.getAtmosphere() != null ? meeting.getAtmosphere().name() : "기타",
                                                meeting.getCategory() != null ? meeting.getCategory().name() : "미정"),

                                // ◀ [중요] 공지사항도 실제 DB 필드값을 보냅니다.
                                meeting.getHostAnnouncement() != null ? meeting.getHostAnnouncement()
                                                : "등록된 공지사항이 없습니다.",

                                meeting.getUser().getId(),
                                meeting.getUser().getNickname(),

                                // ◀ [중요] 아까 만든 참여자 목록과 내 상태 정보를 넣어줍니다.
                                new MeetingDetailResponse.ParticipantInfo(
                                                memberDetails.size(), // APPROVED 된 인원수만 계산
                                                meeting.getMaxParticipants(),
                                                memberDetails),
                                myStatus != null ? new MeetingDetailResponse.UserStatus(
                                                myStatus.isHost(),
                                                myStatus.getStatus().name()) : null);
        }

        @Transactional(readOnly = true)
        public modak.modakmodak.dto.MeetingListResponse getMeetingList(Long userId) {
                List<Meeting> meetings = meetingRepository.findAll();

                List<modak.modakmodak.dto.MeetingDto> meetingDtos = meetings.stream().map(meeting -> {
                        modak.modakmodak.entity.User hostUser = meeting.getUser();
                        Long hostId = (hostUser != null) ? hostUser.getId() : null;
                        String hostNickname = (hostUser != null) ? hostUser.getNickname() : "알수없음";

                        int count = participantRepository.countByMeetingId(meeting.getId());

                        // location 필드: area만 사용
                        String location = meeting.getArea() != null ? meeting.getArea() : "";

                        return new modak.modakmodak.dto.MeetingDto(
                                        meeting.getId(),
                                        meeting.getTitle(),
                                        meeting.getCreatedAt() != null ? meeting.getCreatedAt().toString() : "",
                                        meeting.getImageUrl() != null ? meeting.getImageUrl() : "pod_1.png",
                                        hostId,
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

                // 오늘의 팟 로직 수정: 로그인한 유저가 참여(APPROVED) 중이고, 날짜가 '오늘'인 팟 조회
                modak.modakmodak.dto.TodayMeetingDto todayData = null;
                java.time.LocalDate today = java.time.LocalDate.now();

                // 1. 유저가 참여한 APPROVED 상태의 모든 참가 정보 조회 (DB 쿼리 최적화 필요하지만 일단 로직 구현 우선)
                List<Participant> myParticipations = participantRepository.findAll().stream()
                                .filter(p -> p.getUser().getId().equals(userId) &&
                                                p.getStatus() == modak.modakmodak.entity.ParticipationStatus.APPROVED)
                                .toList();

                // 2. 그 중 날짜가 오늘이고, 완료되지 않은 팟 찾기 (Participant 자체를 찾음)
                java.util.Optional<Participant> myTodayParticipation = myParticipations.stream()
                                .filter(p -> {
                                        Meeting m = p.getMeeting();
                                        return m.getDate() != null &&
                                                        m.getDate().toLocalDate().isEqual(today) &&
                                                        (m.getIsCompleted() == null || !m.getIsCompleted());
                                })
                                .findFirst();

                if (myTodayParticipation.isPresent()) {
                        Participant p = myTodayParticipation.get();
                        Meeting meeting = p.getMeeting();
                        String goal = p.getGoal() != null ? p.getGoal() : "";

                        todayData = new modak.modakmodak.dto.TodayMeetingDto(
                                        meeting.getId(),
                                        meeting.getArea() != null ? meeting.getArea() : "미정", // spot
                                        meeting.getTitle(),
                                        meeting.getDate() != null ? meeting.getDate().toString() : "",
                                        goal,
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

                // 4. 정원 초과 확인 (승인된 멤버만 카운트)
                int currentCount = participantRepository.countByMeetingIdAndStatus(meetingId,
                                modak.modakmodak.entity.ParticipationStatus.APPROVED);
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

                // 6. 팟장에게 알림 생성
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host != null && host.getUser() != null) {
                        modak.modakmodak.entity.Notification notification = modak.modakmodak.entity.Notification
                                        .builder()
                                        .user(host.getUser()) // 알림 받을 사용자 (팟장)
                                        .type(modak.modakmodak.entity.NotificationType.MEETING_APPLICATION)
                                        .title("") // title은 사용하지 않음 (프론트에서 senderNickname 사용)
                                        .message("") // message는 프론트에서 처리
                                        .senderNickname(user.getNickname()) // 신청자의 닉네임
                                        .podName(meeting.getTitle()) // 팟 이름
                                        .profileImage(user.getProfileImage()) // 신청자의 프로필 이미지
                                        .relatedId(meetingId) // 관련 팟 ID
                                        .applicationId(participant.getId()) // 신청서 ID (Participant ID)
                                        .isRead(false)
                                        .build();
                        notificationRepository.save(notification);
                }

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

                // 요청자가 해당 모임의 방장인지 확인
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("승인 권한이 없습니다 (방장이 아닙니다).");
                }

                // 1. 신청서 조회
                modak.modakmodak.entity.Participant participant = participantRepository.findById(applicationId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "존재하지 않는 신청서입니다. ID: " + applicationId));

                // 모임 ID 검증
                if (!participant.getMeeting().getId().equals(meetingId)) {
                        throw new IllegalArgumentException("해당 모임의 신청서가 아닙니다.");
                }

                // 상태 업데이트
                modak.modakmodak.entity.ParticipationStatus newStatus = modak.modakmodak.entity.ParticipationStatus
                                .valueOf(request.status());
                participant.updateStatus(newStatus);

                if (newStatus == modak.modakmodak.entity.ParticipationStatus.APPROVED) {
                        participant.updateGoal(null); // 방장이 승인하면 목표를 일단 null로 초기화
                }

                // APPROVED 인 경우 정원 체크 (신청 시에도 체크하지만, 동시성 문제 등 대비)
                if (newStatus == modak.modakmodak.entity.ParticipationStatus.APPROVED) {
                        int currentCount = participantRepository.countByMeetingIdAndStatus(meetingId,
                                        modak.modakmodak.entity.ParticipationStatus.APPROVED);
                        if (currentCount >= participant.getMeeting().getMaxParticipants()) {
                                // 이미 정원 초과라면 다시 롤백하거나 예외 발생
                                throw new IllegalArgumentException("정원이 초과되어 승인할 수 없습니다.");
                        }
                }

                // 변경 사항 저장 (Dirty Checking으로 자동 저장되지만 명시적으로 호출해도 무방)
                participantRepository.save(participant);

                // 현재 인원 다시 계산
                int updatedCount = participantRepository.countByMeetingIdAndStatus(meetingId,
                                modak.modakmodak.entity.ParticipationStatus.APPROVED);
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
        }

        @Transactional
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

                // [Fix] 출석률 실시간 업데이트
                if (participant.getUser() != null) {
                        updateUserAttendanceRate(participant.getUser());
                }

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

        private void updateUserAttendanceRate(modak.modakmodak.entity.User user) {
                if (user == null)
                        return;

                // 1. 해당 유저가 참여(APPROVED)한 모든 모임 수
                int totalApproved = participantRepository.countByUserIdAndStatus(user.getId(),
                                modak.modakmodak.entity.ParticipationStatus.APPROVED);

                // 2. 그 중 출석(Attended=true)한 모임 수
                int totalAttended = participantRepository.countByUserIdAndStatusAndAttendedTrue(user.getId(),
                                modak.modakmodak.entity.ParticipationStatus.APPROVED);

                // 3. 출석률 계산 (소수점 한자리까지 반올림 예: 33.3)
                float rate = 0.0f;
                if (totalApproved > 0) {
                        rate = (float) totalAttended / totalApproved * 100;
                        rate = Math.round(rate * 10) / 10.0f;
                }

                // 4. 유저 정보 업데이트
                user.setAttendanceRate(rate);
                userRepository.save(user); // 명시적 저장
        }

        @Transactional
        public void updateHostAnnouncement(Long userId, Long meetingId, String announcement) {
                Meeting meeting = findAndValidateHost(userId, meetingId); // 공통 검증 로직 호출
                meeting.setHostAnnouncement(announcement); // 엔티티에 setter 혹은 update 메서드 필요
        }

        @Transactional
        public void updateDate(Long userId, Long meetingId, String date) {
                Meeting meeting = findAndValidateHost(userId, meetingId);
                if (date != null)
                        meeting.setDate(LocalDateTime.parse(date));
        }

        @Transactional
        public void updateLocationDetail(Long userId, Long meetingId, String detail) {
                Meeting meeting = findAndValidateHost(userId, meetingId);
                meeting.setLocationDetail(detail);
        }

        // [꿀팁] 방장 권한 체크 로직이 반복되니 별도 메서드로 빼면 깔끔해요!
        private Meeting findAndValidateHost(Long userId, Long meetingId) {
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);

                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("수정 권한이 없습니다.");
                }
                return meeting;
        }

        @Transactional
        public void removeParticipant(Long userId, Long meetingId, Long participantId) {
                // 1. 요청자가 호스트인지 확인
                modak.modakmodak.entity.Participant host = participantRepository
                                .findByMeetingIdAndIsHostTrue(meetingId);
                if (host == null || !host.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("참가자 삭제 권한이 없습니다 (팟장이 아닙니다).");
                }

                // 2. 삭제할 참가자 조회
                modak.modakmodak.entity.Participant participant = participantRepository.findById(participantId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 참가자입니다."));

                // 3. 해당 모임의 참가자인지 확인
                if (!participant.getMeeting().getId().equals(meetingId)) {
                        throw new IllegalArgumentException("해당 모임의 참가자가 아닙니다.");
                }

                // 4. 호스트는 삭제할 수 없음
                if (participant.isHost()) {
                        throw new IllegalArgumentException("팟장은 삭제할 수 없습니다.");
                }

                // 5. 참가자 삭제
                participantRepository.delete(participant);
        }
}

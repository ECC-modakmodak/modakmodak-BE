package modak.modakmodak.meeting;

import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.dto.MeetingSetupRequest;
import modak.modakmodak.entity.Meeting;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;
import modak.modakmodak.entity.Participant;
import modak.modakmodak.entity.ParticipationStatus;
import modak.modakmodak.entity.User;
import modak.modakmodak.repository.NotificationRepository;
import modak.modakmodak.repository.ParticipantRepository;
import modak.modakmodak.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

        @InjectMocks
        private MeetingService meetingService;

        @Mock
        private MeetingRepository meetingRepository;
        @Mock
        private ParticipantRepository participantRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private NotificationRepository notificationRepository;

        private User user;
        private Meeting meeting;
        private Participant hostParticipant;

        @BeforeEach
        void setUp() {
                user = User.builder()
                                .id(1L)
                                .nickname("testUser")
                                .build();

                // 1단계 설정값
                meeting = Meeting.builder()
                                .id(1L)
                                .user(user)
                                .atmosphere(MeetingAtmosphere.CHATTY)
                                .category(MeetingCategory.CAFE)
                                .categoryEtc("기타")
                                .maxParticipants(4)
                                .status("PENDING")
                                .build();

                hostParticipant = Participant.builder()
                                .id(1L)
                                .user(user)
                                .meeting(meeting)
                                .status(ParticipationStatus.APPROVED)
                                .isHost(true)
                                .build();
        }

        @Test
        @DisplayName("2단계 설정 시 imageUrl이 정상적이고 선택 항목이 null일 때 기존 설정값 유지되어야 함")
        void completeMeeting_WithValidImage_ShouldPreserveOptionalFields() {
                // given
                Long userId = 1L;
                Long meetingId = 1L;

                // 2단계 요청 (imageUrl 정상, 나머지 선택 항목 null)
                MeetingDetailRequest request = new MeetingDetailRequest(
                                "Updated Title",
                                "2026-02-20T10:00:00",
                                "Seoul",
                                "Gangnam",
                                "Description",
                                "https://example.com/image.png", // Valid Image URL
                                "Announcement",
                                null, // Atmosphere (should remain CHATTY)
                                null, // Category (should remain CAFE)
                                null, // CategoryEtc (should remain "기타")
                                0 // MaxParticipants (should remain 4)
                );

                given(meetingRepository.findById(meetingId)).willReturn(Optional.of(meeting));
                given(participantRepository.findByMeetingIdAndIsHostTrue(meetingId)).willReturn(hostParticipant);

                // when
                meetingService.completeMeeting(userId, meetingId, request);

                // then
                // 1. Image URL should be updated
                assertThat(meeting.getImageUrl()).isEqualTo("https://example.com/image.png");

                // 2. Optional fields should be preserved (from setUp)
                assertThat(meeting.getAtmosphere()).isEqualTo(MeetingAtmosphere.CHATTY);
                assertThat(meeting.getCategory()).isEqualTo(MeetingCategory.CAFE);
                assertThat(meeting.getCategoryEtc()).isEqualTo("기타");
                assertThat(meeting.getMaxParticipants()).isEqualTo(4);
        }

        @Test
        @DisplayName("메인 화면 조회 시 오늘의 팟 목표가 정상적으로 반환되어야 함")
        void getMeetingList_ShouldReturnTodayMeetingGoal() {
                // given
                Long userId = 1L;
                String expectedGoal = "오늘의 목표는 코딩!";

                // 오늘 날짜로 설정
                meeting.setDate(java.time.LocalDateTime.now());

                // 참여자 정보에 목표 설정
                hostParticipant.updateGoal(expectedGoal);

                given(meetingRepository.findAll()).willReturn(java.util.List.of(meeting));
                given(participantRepository.findAll()).willReturn(java.util.List.of(hostParticipant));
                given(participantRepository.findByMeetingIdAndIsHostTrue(any())).willReturn(hostParticipant);
                given(participantRepository.countByMeetingIdAndStatus(any(), any())).willReturn(1);

                // when
                modak.modakmodak.dto.MeetingListResponse response = meetingService.getMeetingList(userId);

                // then
                assertThat(response.todayData()).isNotNull();
                assertThat(response.todayData().goal()).isEqualTo(expectedGoal);
        }
}

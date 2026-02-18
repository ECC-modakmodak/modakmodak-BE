package modak.modakmodak.meeting;

import modak.modakmodak.dto.AttendanceCheckRequest;
import modak.modakmodak.dto.AttendanceCheckResponse;
import modak.modakmodak.entity.Meeting;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingCategory;
import modak.modakmodak.entity.Participant;
import modak.modakmodak.entity.ParticipationStatus;
import modak.modakmodak.entity.User;
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
class AttendanceTest {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private UserRepository userRepository;

    private User user;
    private User hostUser;
    private Meeting meeting;
    private Participant hostParticipant;
    private Participant userParticipant;

    @BeforeEach
    void setUp() {
        hostUser = User.builder().id(1L).nickname("Host").build();
        user = User.builder().id(2L).nickname("Guest").attendanceRate(0.0f).build();

        meeting = Meeting.builder()
                .id(1L)
                .user(hostUser)
                .title("Test Meeting")
                .build();

        hostParticipant = Participant.builder()
                .id(1L)
                .user(hostUser)
                .meeting(meeting)
                .isHost(true)
                .status(ParticipationStatus.APPROVED)
                .build();

        userParticipant = Participant.builder()
                .id(2L)
                .user(user)
                .meeting(meeting)
                .isHost(false)
                .status(ParticipationStatus.APPROVED)
                .attended(false)
                .build();
    }

    @Test
    @DisplayName("출석 체크 시 유저의 출석률이 업데이트되어야 함 (50% -> 1/2)")
    void checkAttendance_ShouldUpdateAttendanceRate() {
        // given
        Long userId = hostUser.getId();
        Long meetingId = meeting.getId();
        AttendanceCheckRequest request = new AttendanceCheckRequest(userParticipant.getId(), true);

        given(participantRepository.findByMeetingIdAndIsHostTrue(meetingId)).willReturn(hostParticipant);
        given(participantRepository.findById(userParticipant.getId())).willReturn(Optional.of(userParticipant));

        // Mocking counts for rate calculation
        // Total approved: 2, Total attended: 1 (current one) => 50%
        given(participantRepository.countByUserIdAndStatus(user.getId(), ParticipationStatus.APPROVED)).willReturn(2);
        given(participantRepository.countByUserIdAndStatusAndAttendedTrue(user.getId(), ParticipationStatus.APPROVED))
                .willReturn(1);

        // when
        AttendanceCheckResponse response = meetingService.checkAttendance(userId, meetingId, request);

        // then
        assertThat(response.data().attended()).isTrue();
        verify(userRepository).save(user);
        assertThat(user.getAttendanceRate()).isEqualTo(50.0f);
    }

    @Test
    @DisplayName("결석 처리 시 유저의 출석률이 업데이트되어야 함 (0% -> 0/2)")
    void checkAttendance_Absent_ShouldUpdateAttendanceRate() {
        // given
        Long userId = hostUser.getId();
        Long meetingId = meeting.getId();
        AttendanceCheckRequest request = new AttendanceCheckRequest(userParticipant.getId(), false);

        given(participantRepository.findByMeetingIdAndIsHostTrue(meetingId)).willReturn(hostParticipant);
        given(participantRepository.findById(userParticipant.getId())).willReturn(Optional.of(userParticipant));

        // Mocking counts for rate calculation
        // Total approved: 2, Total attended: 0 => 0%
        given(participantRepository.countByUserIdAndStatus(user.getId(), ParticipationStatus.APPROVED)).willReturn(2);
        given(participantRepository.countByUserIdAndStatusAndAttendedTrue(user.getId(), ParticipationStatus.APPROVED))
                .willReturn(0);

        // when
        meetingService.checkAttendance(userId, meetingId, request);

        // then
        verify(userRepository).save(user);
        assertThat(user.getAttendanceRate()).isEqualTo(0.0f);
    }
}

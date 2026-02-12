package modak.modakmodak.meeting;

import modak.modakmodak.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import modak.modakmodak.meeting.MeetingRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
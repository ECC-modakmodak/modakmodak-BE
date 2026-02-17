package modak.modakmodak.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import modak.modakmodak.dto.MeetingDetailRequest;
import modak.modakmodak.meeting.MeetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingService meetingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("2단계 설정 시 imageUrl이 있고 나머지 null일 때, Service에 null로 전달되어야 함")
    void complete_WithValidImage_ShouldPassNullsToService() throws Exception {
        // given
        Long userId = 1L;
        Long meetingId = 1L;

        String jsonContent = """
                    {
                        "imageUrl": "https://example.com/image.png",
                        "atmosphere": null,
                        "category": null,
                        "categoryEtc": null,
                        "maxParticipants": 0,
                        "title": "New Title",
                        "date": "2026-02-20T10:00:00",
                        "area": "Seoul",
                        "locationDetail": "Gangnam",
                        "description": "Desc",
                        "hostAnnouncement": "Announce"
                    }
                """;

        // when
        mockMvc.perform(post("/api/meetings/{meetingId}/details", meetingId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk());

        // then
        MeetingDetailRequest expectedRequest = new MeetingDetailRequest(
                "New Title",
                "2026-02-20T10:00:00",
                "Seoul",
                "Gangnam",
                "Desc",
                "https://example.com/image.png",
                "Announce",
                null, // atmosphere
                null, // category
                null, // categoryEtc
                0 // maxParticipants
        );

        verify(meetingService).completeMeeting(eq(userId), eq(meetingId), refEq(expectedRequest));
    }
}

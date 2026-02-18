package modak.modakmodak.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import modak.modakmodak.dto.UserJoinRequest;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingMethod;
import modak.modakmodak.service.UserService;
import modak.modakmodak.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.servlet.ServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true",
        "logging.level.org.hibernate.tool.schema=DEBUG",
        "logging.level.org.hibernate.SQL=DEBUG",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=true",
        "spring.jpa.generate-ddl=true"
})
@AutoConfigureMockMvc
class UserSignupTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 테스트 - 유효한 정보 입력 시 200 OK 반환")
    void signup_Success() throws Exception {
        // given
        UserJoinRequest request = new UserJoinRequest(
                "testuser123",
                "Password123!", // Valid password: Letters, Numbers, Special Chars, 8-20 length
                "test@example.com",
                "TestNickname",
                MeetingAtmosphere.CHATTY,
                MeetingMethod.대면,
                "Seoul",
                "My Target");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 비밀번호 규칙 위반 (특수문자 누락)")
    void signup_Fail_InvalidPassword() {
        // given
        UserJoinRequest request = new UserJoinRequest(
                "testuser456",
                "Password123", // Missing special char
                "test2@example.com",
                "TestNickname2",
                MeetingAtmosphere.QUIET,
                MeetingMethod.비대면,
                "Busan",
                "Target");

        // when & then
        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(post("/api/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print());
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }
}

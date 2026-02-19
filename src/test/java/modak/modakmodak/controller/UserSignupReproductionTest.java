package modak.modakmodak.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import modak.modakmodak.dto.UserJoinRequest;
import modak.modakmodak.entity.MeetingAtmosphere;
import modak.modakmodak.entity.MeetingMethod;
import modak.modakmodak.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@Transactional
public class UserSignupReproductionTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void signup_WithKoreanEnums_ShouldWork() throws Exception {
                // Given: User input exactly as provided in the issue
                // "preferredMethod": "대면"
                UserJoinRequest request = new UserJoinRequest(
                                "1234",
                                "pw123456!",
                                "123@example.com",
                                "12",
                                MeetingAtmosphere.CHATTY,
                                MeetingMethod.대면, // Korean Enum value
                                "서울시 서대문구",
                                "웹 개발 정복하기!");

                // When & Then
                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        void signup_DuplicateEmail_ShouldFail_With500() throws Exception {
                // 1. First signup
                UserJoinRequest request1 = new UserJoinRequest(
                                "uniqUser1",
                                "pw123456!",
                                "dup@example.com",
                                "uniqNick1",
                                MeetingAtmosphere.CHATTY,
                                MeetingMethod.대면,
                                "서울시 서대문구",
                                "웹 개발 정복하기!");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                                .andExpect(status().isOk());

                // 2. Second signup with SAME EMAIL but unique username/nickname
                // Controller checks username/nickname but NOT email. Service checks email and
                // throws IllegalStateException.
                UserJoinRequest request2 = new UserJoinRequest(
                                "uniqUser2",
                                "pw123456!",
                                "dup@example.com", // Duplicate email
                                "uniqNick2",
                                MeetingAtmosphere.CHATTY,
                                MeetingMethod.대면,
                                "서울시 서대문구",
                                "웹 개발 정복하기!");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andDo(print())
                                .andExpect(status().isBadRequest()) // Now expecting 400
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 가입된 이메일입니다."))); // Optional:
                                                                                                                      // Verify
                                                                                                                      // message
        }

        @Test
        void signup_InvalidPassword_ShouldFail_With400() throws Exception {
                UserJoinRequest request = new UserJoinRequest(
                                "validUser",
                                "weakpw", // Invalid password (too short, no special char)
                                "valid@example.com",
                                "validNick",
                                MeetingAtmosphere.CHATTY,
                                MeetingMethod.대면,
                                "서울시 서대문구",
                                "웹 개발 정복하기!");

                // Expecting 400 Bad Request because IllegalArgumentException is now handled
                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(content()
                                                .string(org.hamcrest.Matchers.containsString("비밀번호는 영문자, 숫자, 특수문자")));
        }

        @Test
        void signup_DuplicateNickname_ShouldFail_With400() throws Exception {
                UserJoinRequest request = new UserJoinRequest(
                                "user1", "pw123456!", "email1@test.com", "duplicateNick",
                                MeetingAtmosphere.CHATTY, MeetingMethod.대면, "Area", "Goal");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                UserJoinRequest request2 = new UserJoinRequest(
                                "user2", "pw123456!", "email2@test.com", "duplicateNick", // Duplicate Nickname
                                MeetingAtmosphere.CHATTY, MeetingMethod.대면, "Area", "Goal");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 사용 중인 닉네임입니다.")));
        }

        @Test
        void signup_DuplicateUsername_ShouldFail_With400() throws Exception {
                UserJoinRequest request = new UserJoinRequest(
                                "duplicateUser", "pw123456!", "email1@test.com", "nick1",
                                MeetingAtmosphere.CHATTY, MeetingMethod.대면, "Area", "Goal");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                UserJoinRequest request2 = new UserJoinRequest(
                                "duplicateUser", "pw123456!", "email2@test.com", "nick2", // Duplicate Username
                                MeetingAtmosphere.CHATTY, MeetingMethod.대면, "Area", "Goal");

                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 사용 중인 아이디입니다.")));
        }
}

-- ID 1번으로 고정하여 '지인' 유저를 생성합니다.
INSERT INTO users (id, username, password, email, nickname, preferred_type, preferred_method, activity_area, target_message, attendance_rate)
VALUES (1, 'try2', 'pw123456', 'try2@example.com', '지인', 'CHATTY', '대면', '서울시 서대문구', '웹 개발 정복하기!', 0);

-- ID 1번으로 고정하고, 작성자(user_id)를 1번 유저('지인')로 연결합니다.
INSERT INTO meeting (id, user_id, title, category, atmosphere, max_participants, area, location_detail, date, status, description)
VALUES (1, 1, '모닥모닥 첫 모임', 'STUDY', 'CHATTY', 5, '서울시 서대문구', '이화여대 ECC 1번 테이블', '2026-02-20T19:00:00', 'AVAILABLE', '함께 열심히 코딩해요!');
-- 1. 유저 생성
INSERT INTO users (id, username, password, email, nickname, profile_image, preferred_type, preferred_method, activity_area, target_message, attendance_rate)
VALUES (1, 'try2', 'pw123456', 'try2@example.com', '지인',
        'profile_default.png',
        'CHATTY', '대면', '서울시 서대문구', '웹 개발 정복하기!', 0);

INSERT INTO users (id, username, password, email, nickname, profile_image, preferred_type, preferred_method, activity_area, target_message, attendance_rate)
VALUES (2, 'potato', 'pw123456', 'potato@example.com', '감자',
        'profile_default.png',
        'CHATTY', '대면', '서울시 서대문구', '웹 개발 정복하기!', 0);

-- 2. 모임 생성 (host_announcement 컬럼을 추가해야 합니다!)
INSERT INTO meeting (id, user_id, title, category, atmosphere, max_participants, area, location_detail, host_announcement, date, status, description, image_url)
VALUES (1, 1, '모닥모닥 첫 모임', 'CAFE', 'CHATTY', 5, '서울시 서대문구', '이화여대 ECC 1번 테이블',
        '방장이 등록한 공지사항이 이곳에 표시됩니다.',
        '2026-02-20T19:00:00', 'AVAILABLE', '함께 열심히 코딩해요!',
        'pod_1.png');

-- 3. 참여자 연결
INSERT INTO participant (meeting_id, user_id, status)
VALUES (1, 1, 'HOST');

INSERT INTO participant (meeting_id, user_id, status)
VALUES (1, 2, 'PARTICIPANT');

CREATE TABLE `Member` (
	`member_id`	BIGINT	NOT NULL	COMMENT '사용자 고유 번호',
	`email`	VARCHAR(50)	NOT NULL	COMMENT '사용자 이메일',
	`password_hash`	VARCHAR(255)	NOT NULL	COMMENT '사용자 비밀번호',
	`age`	INT	NULL	COMMENT '사용자 나이',
	`address`	VARCHAR(255)	NULL	COMMENT '사용자 주소',
	`gender`	VARCHAR(6)	NULL	DEFAULT MALE, FEMALE, ETC	COMMENT '사용자 성별',
	`phone_number`	VARCHAR(13)	NULL	COMMENT '사용자 핸드폰 번호',
	`role`	VARCHAR(20)	NOT NULL	DEFAULT GENERAL, ADMIN	COMMENT '사용자 역할',
	`position`	VARCHAR(50)	NULL	COMMENT '사용자 직책/포지션',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`deleted_at`	DATETIME	NULL	COMMENT '삭제 일자',
	`provider_id`	BIGINT	NOT NULL	COMMENT '가입 방법 고유 번호'
);

CREATE TABLE `Certification` (
	`certification_id`	BIGINT	NOT NULL	COMMENT '수료증 고유 번호',
	`qualification_id`	VARCHAR(100)	NOT NULL	COMMENT '자격 아이디',
	`certificated_hash`	VARCHAR(100)	NOT NULL	COMMENT '수료증 번호(암호화)',
	`certificated_at`	DATETIME	NOT NULL	COMMENT '수료 일자',
	`workspace_member_finished_id`	BIGINT	NOT NULL	COMMENT '강의 수료생 고유 번호',
	`issuing_agency`	VARCHAR(50)	NOT NULL	COMMENT '발급 기관'
);

CREATE TABLE `QnARoom` (
	`qna_room_id`	CHAR(36)	NOT NULL	COMMENT '질의응답방 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호'
);

CREATE TABLE `Session` (
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`title`	VARCHAR(50)	NOT NULL	COMMENT '세션 이름',
	`description`	VARCHAR(255)	NULL	COMMENT '세션 설명',
	`number`	INT	NOT NULL	COMMENT '세션 번호',
	`start_at`	TIME	NOT NULL	COMMENT '세션 시작 시간',
	`date`	DATE	NOT NULL	COMMENT '세션 일자',
	`end_at`	TIME	NOT NULL	COMMENT '세션 종료 시간',
	`attendance_excused`	DECIMAL(8, 2)	NOT NULL	COMMENT '출석 인정 기준',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호',
	`janus_room_id`	BIGINT	NULL	COMMENT '화상 채팅 방 번호',
	`pin`	VARCHAR(255)	NULL	COMMENT '화상 채팅 핀 번호'
);

CREATE TABLE `FeelingGraph` (
	`feeling_id`	BIGINT	NOT NULL	COMMENT '분위기 고유번호',
	`name`	VARCHAR(31)	NOT NULL	COMMENT '감정 이름',
	`en_name`	VARCHAR(31)	NOT NULL	COMMENT '감정 영어 이름',
	`value`	INT	NULL	COMMENT '감정값',
	`created_at`	DATETIME	NOT NULL	COMMENT '생성시간',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호'
);

CREATE TABLE `WorkspaceRegister` (
	`workspace_register_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 가입 대기 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`approved_at`	DATETIME	NULL	COMMENT '승인 일자',
	`status`	VARCHAR(10)	NOT NULL	DEFAULT PENDING, APPROVE, REJECTED	COMMENT '가입 승인 상태',
	`reject_reason`	VARCHAR(1000)	NULL	COMMENT '반려 사유',
	`reject_type_id`	BIGINT	NOT NULL	COMMENT '반려 유형 고유 번호',
	`member_id`	BIGINT	NOT NULL	COMMENT '사용자 고유 번호',
	`workspace_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 고유 번호'
);

CREATE TABLE `CourseLearner` (
	`workspace_member_course_id`	BIGINT	NOT NULL	COMMENT '강의 수강생 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여진 고유 번호'
);

CREATE TABLE `ExitQuizAnswer` (
	`exit_quiz_answer_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 답안 고유 번호',
	`number`	INT	NOT NULL	COMMENT '답안 번호',
	`answer`	VARCHAR(255)	NOT NULL	COMMENT '답안 내용',
	`exit_quiz_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 고유 번호'
);

CREATE TABLE `AiDeadzoneQuiz` (
	`quiz_id`	BIGINT	NOT NULL	COMMENT '퀴즈 고유번호',
	`start_time`	INT	NOT NULL	COMMENT '시작시간',
	`end_time`	INT	NOT NULL	COMMENT '종료시간',
	`question`	VARCHAR(255)	NOT NULL	COMMENT '퀴즈 질문',
	`answer_number`	INT	NOT NULL	COMMENT '퀴즈 정답 번호',
	`created_at`	DATETIME	NOT NULL	COMMENT '생성 일자',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호'
);

CREATE TABLE `ReviewChattingMessage` (
	`chatting_message_id`	BIGINT	NOT NULL	COMMENT '채팅 메시지 고유 번호',
	`message`	VARCHAR(1000)	NOT NULL	COMMENT '메시지 내용',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`room_id`	CHAR(36)	NOT NULL	COMMENT '채팅방 고유번호',
	`workspace_member_id`	BIGINT	NULL	COMMENT '보낸사람(null이면 AI)'
);

CREATE TABLE `CourseCategory` (
	`course_category_id`	BIGINT	NOT NULL	COMMENT '교육 과정 종류 고유 번호',
	`course_category_name`	VARCHAR(50)	NOT NULL	COMMENT '교육 과정 종류 이름'
);

CREATE TABLE `CourseCertificated` (
	`workspace_member_certificated_id`	BIGINT	NOT NULL	COMMENT '강의 수료생 고유 번호',
	`certificated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '수료 일자',
	`score`	INT	NOT NULL	COMMENT '점수',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여진 고유 번호',
	`average_participate`	DECIMAL(8, 2)	NOT NULL	COMMENT '평균 참여도',
	`quiz_correct`	DECIMAL(8, 2)	NOT NULL	COMMENT '퀴즈 정답률',
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호'
);

CREATE TABLE `CourseApply` (
	`course_apply_id`	BIGINT	NOT NULL	COMMENT '수강 신청 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`is_applied`	TINYINT	NULL	COMMENT '승인 여부',
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여진 고유 번호'
);

CREATE TABLE `CourseLearnerSession` (
	`workspace_member_session_id`	BIGINT	NOT NULL	COMMENT '세션 이수 정보 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`exit_ticket`	INT	NULL	COMMENT '퇴실 퀴즈 점수',
	`is_attendance`	TINYINT	NOT NULL	COMMENT '출석 여부',
	`workspace_member_course_id`	BIGINT	NOT NULL	COMMENT '강의 수강생 고유 번호',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호'
);

CREATE TABLE `Script` (
	`script_id`	BIGINT	NOT NULL	COMMENT '스크립트 id',
	`time`	INT	NOT NULL	COMMENT '측정시간',
	`message`	VARCHAR(1000)	NOT NULL	COMMENT '강의 스크립트',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호'
);

CREATE TABLE `WorkspaceMember` (
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호',
	`connected_at`	DATETIME	NULL	COMMENT '최근 접속일',
	`status`	VARCHAR(20)	NOT NULL	DEFAULT ACTIVE, INACTIVE	COMMENT '상태',
	`role`	VARCHAR(20)	NOT NULL	DEFAULT LEARNER, TRAINER	COMMENT '참여자 역할',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`member_id`	BIGINT	NOT NULL	COMMENT '관리자 고유 번호',
	`workspace_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 고유 번호'
);

CREATE TABLE `Workspace` (
	`workspace_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 고유 번호',
	`name`	VARCHAR(100)	NOT NULL	COMMENT '워크스페이스 이름',
	`description`	VARCHAR(300)	NULL	COMMENT '워크스페이스 설명',
	`logo_url`	VARCHAR(1000)	NULL	COMMENT '워크스페이스 로고 URL',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`deleted_at`	DATETIME	NULL	COMMENT '삭제 일자',
	`is_searchable`	TINYINT	NOT NULL	COMMENT '워크스페이스 검색 가능 여부',
	`member_id`	BIGINT	NOT NULL	COMMENT '관리자 고유 번호'
);

CREATE TABLE `RejectType` (
	`reject_type_id`	BIGINT	NOT NULL	COMMENT '반려 유형 고유 번호',
	`reject_type_name`	VARCHAR(50)	NOT NULL	COMMENT '반려 유형 이름'
);

CREATE TABLE `AiAnalysisTimeline` (
	`concentrate_id`	BIGINT	NOT NULL	COMMENT '몰입도 고유번호',
	`time`	INT	NOT NULL	COMMENT '측정시간',
	`deadzone`	TINYINT	NOT NULL	DEFAULT true / false	COMMENT '데드존 표시',
	`value`	INT	NOT NULL	COMMENT '몰입도 값',
	`feeling_name`	VARCHAR(31)	NOT NULL	COMMENT '분위기 종류',
	`en_feeling_name`	VARCHAR(31)	NOT NULL	COMMENT '영어 분위기 종류',
	`fevertime`	TINYINT	NOT NULL	DEFAULT true / false	COMMENT '피버타임 유무',
	`created_at`	DATETIME	NOT NULL	COMMENT '생성시간',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호'
);

CREATE TABLE `AIQuizAnswer` (
	`exit_quiz_answer_id`	BIGINT	NOT NULL	COMMENT 'AI  퀴즈 답안 고유 번호',
	`number`	INT	NOT NULL	COMMENT '답안 번호',
	`answer`	VARCHAR(255)	NOT NULL	COMMENT '답안 내용',
	`quiz_id`	BIGINT	NOT NULL	COMMENT '퀴즈 고유번호'
);

CREATE TABLE `AuthProvider` (
	`provider_id`	BIGINT	NOT NULL	COMMENT '가입 방법 고유 번호',
	`provider_name`	VACHAR(100)	NOT NULL	COMMENT '가입 이름'
);

CREATE TABLE `ExitQuiz` (
	`exit_quiz_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 고유 번호',
	`question`	VARCHAR(255)	NOT NULL	COMMENT '퀴즈 문제',
	`answer_number`	INT	NOT NULL	COMMENT '퀴즈 정답 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`exit_quiz_type_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 답안 고유 번호'
);

CREATE TABLE `InviteCode` (
	`invitecode_id`	BIGINT	NOT NULL	COMMENT '초대코드 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`expired_at`	DATETIME	NULL	COMMENT '만료 일자',
	`code_name`	VARCHAR(50)	NOT NULL	COMMENT '코드명',
	`identify_value`	VARCHAR(50)	NOT NULL	COMMENT '식별값',
	`workspace_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 고유 번호'
);

CREATE TABLE `ExitQuizResult` (
	`result_id`	BIGINT	NOT NULL	COMMENT '퀴즈결과 고유번호',
	`result_number`	INT	NULL	COMMENT '수강생이 고른 답',
	`result`	TINYINT	NOT NULL	DEFAULT true / false	COMMENT '맞았는지 여부',
	`exit_quiz_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호'
);

CREATE TABLE `QnAMessage` (
	`qna_message_id`	BIGINT	NOT NULL	COMMENT '질의응답 메시지 고유 번호',
	`type`	VARCHAR(10)	NOT NULL	DEFAULT Question, Answer	COMMENT '질문/대답 구분',
	`message`	VARCHAR(1000)	NOT NULL	COMMENT '메시지 내용',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`qna_room_id`	CHAR(36)	NOT NULL	COMMENT '질의응답방 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호'
);

CREATE TABLE `Zzim` (
	`zzim_id`	BIGINT	NOT NULL	COMMENT '찜한 강의 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여진 고유 번호',
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호'
);

CREATE TABLE `SessionChattingMessage` (
	`session_chatting_message_id`	BIGINT	NOT NULL	COMMENT '세션 채팅 메시지 고유 번호',
	`message`	VARCHAR(1000)	NOT NULL	COMMENT '메시지 내용',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호',
	`session_chatting_room_id`	CHAR(36)	NOT NULL	COMMENT '세션 채팅방 고유 번호'
);

CREATE TABLE `SessionChattingRoom` (
	`session_chatting_room_id`	CHAR(36)	NOT NULL	COMMENT '세션 채팅방 고유 번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호'
);

CREATE TABLE `LeanerAnalysis` (
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`video_url`	VARCHAR(255)	NULL	COMMENT '다시보기 영상 URL',
	`created_at`	DATETIME	NOT NULL	COMMENT '생성날짜'
);

CREATE TABLE `Course` (
	`course_id`	BIGINT	NOT NULL	COMMENT '강의 고유 번호',
	`title`	VARCHAR(100)	NOT NULL	COMMENT '강의 이름',
	`description`	VARCHAR(255)	NOT NULL	COMMENT '강의 설명',
	`sub_category`	VARCHAR(20)	NOT NULL	COMMENT '상세 분야',
	`maxLearner`	INT	NOT NULL	COMMENT '최대 수강 인원',
	`is_engage_check`	TINYINT	NOT NULL	COMMENT '집중도 체크 여부',
	`is_sentiment_analysis`	TINYINT	NOT NULL	COMMENT '감정 분석 여부',
	`attendance`	INT	NOT NULL	COMMENT '교육과정 수료 조건_필수 출석율',
	`engagement`	INT	NULL	COMMENT '교육과정 수료 조건_평균 집중도',
	`exitQuiz`	INT	NULL	COMMENT '교육과정 수료 조건_퇴실 퀴즈 평균 점수',
	`status`	VARCHAR(20)	NOT NULL	DEFAULT PREPARING, INVITE, PROCEED, FINISH	COMMENT '교육 과정 상태',
	`period`	INT	NOT NULL	COMMENT '교육 기간 (변동 가능)',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`updated_at`	DATETIME	NULL	COMMENT '수정 일자',
	`course_category_id`	BIGINT	NOT NULL	COMMENT '교육 과정 종류 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '강사 고유 번호'
);

CREATE TABLE `ReviewChattingRoom` (
	`room_id`	CHAR(36)	NOT NULL	COMMENT '채팅방 고유번호',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성 일자',
	`session_id`	BIGINT	NOT NULL	COMMENT '강의 세션 고유 번호',
	`workspace_member_id`	BIGINT	NOT NULL	COMMENT '워크스페이스 참여자 고유 번호'
);

CREATE TABLE `ExitQuizType` (
	`exit_quiz_type_id`	BIGINT	NOT NULL	COMMENT '퇴실 퀴즈 답안 고유 번호',
	`type`	VARCHAR(50)	NOT NULL	COMMENT '퀴즈 유형'
);


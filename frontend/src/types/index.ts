export interface Workspace {
  workspaceId: number;
  name: string;
  logoUrl: string | null; // S3 로고 이미지 URL
  description: string;
  memberCount: number;
  courseCount: number;
  myStatus: number | null; // 0: 미가입, 1: 대기, 2: 활동중
  myRole: string | null;
}

export interface CreateCourse {
  workspaceId: number;
  title: string;
  description: string;
  subCategory: string;
  category: string;
  maxLearner: number;
  period: number;
  isEngageCheck: boolean;
  isSentimentAnalysis: boolean;
  attendance: number;
  engagement: number;
  exitQuiz: number;
}

export interface TrainerCourse {
  courseId: number;
  title: string;
  learnerCount: number;
  totalSessionCount: number;
  averageProgressRate: number | null;
  courseStatus: string;
  // 기존 필드들 (필요시 선택적 유지)
  category?: string;
  description?: string;
  status?: string;
}

export interface LearnerCourse {
  // 기본 정보
  courseId: number | string;
  workspaceId: number;
  title: string;
  description: string;
  category: string;
  subCategory?: string;

  // 상태 및 강사 정보 (명세서 기준 추가)
  status: 'PREPARING' | 'PROCEED' | 'FINISH' | 'INVITE';
  ownerName: string;
  isOwner: boolean;

  // 강의 상세 지표 (명세서 2번 기준 추가)
  attendance?: number;      // 출석률
  engagement?: number;      // 집중도
  exitQuiz?: number;        // 종료 퀴즈 점수
  maxLearner?: number;
  period?: number;
  isEngageCheck?: boolean;
  isSentimentAnalysis?: boolean;

  // 프론트엔드 UI용 커스텀 필드 (기존 유지)
  currentLessons?: number;
  totalLessons?: number;
  isLiked?: boolean;
  dDay?: string;
  applyStatus?: string; // 수강 신청 상태 (필요 시 유지)
}

export interface User {
  memberId: number;
  name: string;
  email: string;
  profileImageUrl?: string | null;
  role?: string;
  auth?: string;
}

export interface WorkspaceStats {
  id: number;
  workspaceId: number;
  progress: {
    value: number;
    trend?: string;
    trendType?: "up" | "down" | "info";
  };
  sessions: {
    value: number;
    nextSession?: string | null;
  };
  completed: {
    value: number;
    total: number;
  };
}

export interface Session {
  // ERD 기본 항목
  sessionId: number; // changed from id
  title: string;
  description: string;
  date?: string;
  startAt?: string;
  endAt?: string;
  useFocus?: boolean;
  useEmotion?: boolean;
  useQuiz?: boolean;
  quizCount?: number;
  courseId: number;
  janusRoomId?: number;
  pin?: string;
  state?: "live" | "ended" | "upcoming";
  isCompleted?: boolean;
  duration?: number;
}

export interface Material {
  fileId: number | string;
  fileName: string;
  fileSize: string;
  fileUrl: string;
  fileType: string;
  uploadAt?: string;
}

export interface VideoRoom {
  janusRoomId: number;
  roomAlive: boolean;
  pin: string;
}

export interface LearnerWorkspaceTopCardStats {
  id: number;
  userId: number; // 유저 아이디
  workspaceId: number; // 워크스페이스 아이디
  role: string;
  values: StatItem[];
}

export interface StatItem {
  icon: string;
  title: string;
  unit: string;
  value: string | number;
}
export interface WeeklyStat {
  label: string;
  time: number;
  percentage: number;
  isHighlight?: boolean;
}
export interface SessionConcentrationStats {
  id: number;
  userId: number;
  courseId: number | string;
  workspaceId: number;
  labels: string[];
  myScores: number[];
  avgScores: number[];
  currentFocus: number;
}

export interface SessionReportItem {
  id: number;
  name: string;
  date: string;
  duration: string;
  focus: number;
  participation: string;
  activityScore: number;
}
export interface LearnerSessionReport {
  id: number;
  userId: number;
  workspaceId: number;
  courseId: string;
  sessions: SessionReportItem[];
}

export interface QuizItem {
  id: number;
  type: "single" | "multiple" | "subjective";
  question: string;
  options?: string[];
  correctAnswer: (number | string)[]; // 강사가 설정한 정답
  userAnswer?: (number | string)[]; // [추가] 수강생이 입력한 정답
  timeRange?: string;
  reason?: string;
  isEditing?: boolean;
  isCorrect?: boolean; // [추가] 채점 결과
}

export interface CodeItem {
  joinCodeId: number;
  joinCodeName: string;
  identifyValue: string;
  endedDate: string;
  createdAt?: string; // undefined 허용
  status: "활성" | "만료임박" | "만료";
}

/* ==========================================================
 * WORKSPACE ADMIN TYPE
 * ========================================================== */

/** 1. 초대코드 (Invite Code) */
export interface JoinCode {
  joinCodeId: number;
  identifyValue: string; // 실제 초대 코드
  joinCodeName: string; // 관리용 이름
  endedDate: string; // 만료일 (ISO)
  createdAt?: string; // 생성일 (ISO)
  status?: string; // 클라이언트 계산용 (활성/만료 등)
}

export interface CreateJoinCodePayload {
  workspaceId: number;
  endedDate: string;
  joinCodeName: string;
}

export interface CreateJoinCodeResponse {
  identifyValue: string;
}

/** 2. 가입 신청 (Join Request) */
export interface JoinRequest {
  workspaceRegisterId: number; // PK
  workspaceId: number;
  memberId: number;
  email: string;
  name: string;
  profileImageUrl?: string;
  createdAt: string; // 생성일 (신청일)
  updatedAt?: string; // 수정일 (처리일)
  status: "PENDING" | "APPROVE" | "REJECTED";
  approvedAt?: string; // 승인일
  rejectReason?: string; // 반려 사유
  rejectType?:
  | "AFFLIATION_MISMATCH"
  | "INVITE_CODE_USE_MISS"
  | "OVER_CAPACITY"
  | "ETC"; // 반려 유형
}

/** 3. 워크스페이스 멤버 (Member) */
export interface WorkspaceMember extends User {
  workspaceMemberId: number; // 워크스페이스-멤버 관계의 PK - 백엔드 요청하기
  workspaceId: number | string;

  // 역할 및 상태 (백엔드 Enum 통일)
  role: "TRAINER" | "LEARNER";
  status: "ACTIVE" | "INACTIVE";

  // 날짜 정보 (백엔드 명세서 필드명 준수)
  createdAt: string; // 워크스페이스 가입일
  connectedAt?: string | null; // 최근 접속일 (없을 수 있음)

  // 추가 통계 정보 (선택적)
  courses?: string[];
  stats?: {
    progress: number;
    courseCount: number;
    quizAverage: number;
  };
}

export interface ChangeRoleRequest {
  workspaceMemberId: number;
  workspaceMemberRole: "TRAINER" | "LEARNER"; // 오타 방지용 안전장치
}

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
  // 2026-01-30 확인된 내용, 강사용 코스정보
  courseId: number;
  attendance: number;
  category: string;
  description: string;
  engagement: number;
  exitQuiz: number;
  isEngageCheck: boolean;
  isOwner: boolean;
  isSentimentAnalysis: boolean;
  maxLearner: number;
  ownerName: string;
  period: number;
  status: string;
  subCategory: string;
  title: string;
  workspaceId: number;
}
export interface LearnerCourse {
  // 2026-01-30 확인된 내용, 수강생용 코스정보
  courseId: string | number; // Course.course_id
  workspaceId?: number; //
  title: string;
  description: string;
  category: string;
  subCategory?: string;
  currentLessons: number;
  totalLessons: number;
  isLiked?: boolean;
  dDay?: string;
  // 삭제 enrollmentStatus?: boolean;
  applyStatus: string;
  status?: string;
}

export interface User {
  memberId: number;
  name: string;
  email: string;
  profileImageUrl: string;
  role?: string;
  auth?: string;
}

// 특정 워크스페이스 내에서의 멤버 정보 (Relationship)
export interface WorkspaceMember extends User {
  workspaceId: number | string;
  role: "Admin" | "Instructor" | "Learner"; // 워크스페이스별 역할
  status: "ACTIVE" | "INACTIVE"; // 워크스페이스별 상태
  joinedAt: string;
  lastLogin: string;
  courses?: string[]; // [추가] 수강 중인 강의 목록 (없을 수 있으므로 선택적 속성)
  // 이미지 상세 정보에 필요한 통계
  stats?: {
    progress: number;
    courseCount: number;
    quizAverage: number;
  };
  profileImage?: string; // Added for compatibility
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

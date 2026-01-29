export interface Workspace {
  workspace_id: 1;
  name: "Admin Workspace 1";
  logo_url: "null"; // S3 로고 이미지 URL (이미 반영됨)
  description: "시스템 관리자 운영 공간";
  memberCount: 4;
  courseCount: 5;
  my_status: null; // 검색 가능 여부 워크스페이스
  my_role: string;
}

export interface Course {
  course_id: string | number; // Course.course_id
  workspace_id?: number; //
  title: string;
  description: string;
  sub_category?: string;
  category: string;
  progress_rate: number;
  current_lessons: number;
  total_lessons: number;

  // Frontend specific or projected
  is_mandatory?: boolean;
  d_day?: number;
  is_liked?: boolean;
  duration?: string;
  enrollment_status?: "recruiting" | "pending_approval" | "active";
  course_status?:
    | "PREPARING"
    | "INVITE"
    | "PROCEED"
    | "FINISH"
    | "active"
    | "recruiting"
    | "ended"; // 프론트 임시 상태값 호환용 추가
  request_date?: string;
  tag?: string;
  status?: any;

  // New fields for Course Wizard
  period?: string;
  ai_engagement_check?: boolean;
  ai_sentiment_analysis?: boolean;
  completion_attendance_rate?: number;
  use_engagement_criteria?: boolean;
  completion_engagement_score?: number;
  use_quiz_criteria?: boolean;
  completion_quiz_score?: number;
}

export interface User {
  member_id: number;
  name: string;
  email: string;
  profile_image_url: string;
  role?: string;
  auth?: string;
}

// 특정 워크스페이스 내에서의 멤버 정보 (Relationship)
export interface WorkspaceMember extends User {
  workspace_id: number | string;
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
  workspace_id: number;
  progress: {
    value: number;
    trend?: string;
    trend_type?: "up" | "down" | "info";
  };
  sessions: {
    value: number;
    next_session?: string | null;
  };
  completed: {
    value: number;
    total: number;
  };
}

export interface Session {
  // ERD 기본 항목
  session_id: number; // changed from id
  course_id: number;
  title: string;
  description: string;
  start_at?: string;
  date?: string;
  end_at?: string;
  attendance_time?: Float16Array;
  created_at?: string;
  updated_at?: string;
  janus_room_id?: number;
  pin?: string;

  // 커스텀 영역

  duration?: string;
  is_completed?: boolean;
  state: "live" | "ended" | "upcoming";
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
  janus_room_id: number;
  roomAlive: boolean;
  pin: string;
}

export interface LearnerWorkspaceTopCardStats {
  id: number;
  user_id: number; // 유저 아이디
  workspace_id: number; // 워크스페이스 아이디
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
  user_id: number;
  course_id: number | string;
  workspace_id: number;
  labels: string[];
  my_scores: number[];
  avg_scores: number[];
  current_focus: number;
}

export interface SessionReportItem {
  id: number;
  name: string;
  date: string;
  duration: string;
  focus: number;
  participation: string;
  activity_score: number;
}
export interface LearnerSessionReport {
  id: number;
  user_id: number;
  workspace_id: number;
  course_id: string;
  sessions: SessionReportItem[];
}

export interface QuizItem {
  id: number;
  type: 'single' | 'multiple' | 'subjective';
  question: string;
  options?: string[]; 
  correctAnswer: (number | string)[]; // 강사가 설정한 정답
  userAnswer?: (number | string)[];    // [추가] 수강생이 입력한 정답
  timeRange?: string;
  reason?: string;
  isEditing?: boolean;
  isCorrect?: boolean;                 // [추가] 채점 결과
}
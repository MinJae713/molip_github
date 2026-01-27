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
  course_status?: "PREPARING" | "INVITE" | "PROCEED" | "FINISH";
  request_date?: string;
  tag?: string;
  status?: any;
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
    role: 'Admin' | 'Instructor' | 'Learner'; // 워크스페이스별 역할
    status: 'ACTIVE' | 'INACTIVE';            // 워크스페이스별 상태
    joinedAt: string;
    lastLogin: string;
    // 이미지 상세 정보에 필요한 통계
    stats?: {
        progress: number;
        courseCount: number;
        quizAverage: number;
    };
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
  title: string;
  description: string;
  session_number: number;
  start_at?: string;
  date?: string;
  end_at?: string;
  attendance_excused?: Float16Array;
  created_at?: string;
  updated_at?: string;
  janus_room_id?: number;
  pin?: string;
  course_id: number;
  // 커스텀 영역

  duration?: string;
  type: "live" | "vod" | "quiz" | "assignment";
  is_completed?: boolean;
  order: number;
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
  user_id: number; // 유저 아이디
  workspace_id: number; // 워크스페이스 아이디
  week_study_time: number; // 주간 학습 시간(분단위)
  averageConcentration: number; //평균 집중도
}

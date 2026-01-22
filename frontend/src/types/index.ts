export interface Workspace {
    id: string | number;
    name: string;
    logo_url?: string;
    role?: string;
    status?: number;
    location?: string;
    description?: string;
    memberCount?: number;
    courseCount?: number;
    join_code?: string;
    typeIcon?: string;
    themeColor?: string;
}

export interface Course {
    id: string | number;
    workspace_id?: number;
    title: string;
    description: string;
    tag?: string;
    theme: 'indigo' | 'blue' | 'slate' | 'amber';
    category: string;
    progress: number;
    currentLessons: number;
    totalLessons: number;

    // 추가 필드 (대시보드용)
    is_mandatory?: boolean;
    d_day?: number;
    is_liked?: boolean;
    tag_color?: string; // 'indigo', 'slate', 'amber', 'red' etc.
    sub_tag?: string;
    duration?: string;
    status?: 'recruiting' | 'pending_approval' | 'active';
    request_date?: string;
}

export interface User {
    id: number;
    name: string;
    email: string;
    profileImage: string;
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
        trend_type?: 'up' | 'down' | 'info';
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
    id: number;
    title: string;
    description: string;
    duration?: string; // e.g. "45min"
    type: 'live' | 'vod' | 'quiz' | 'assignment';
    is_completed?: boolean;
    order: number;
}

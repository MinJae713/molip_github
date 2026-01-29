export type Role = 'ADMIN' | 'TRAINER' | 'LEARNER';

export interface SidebarMenuItem {
  id: string;
  name: string;
  icon: string;
  routeName: string;
}

export interface SidebarSection {
  title?: string;   // 섹션 제목 (없으면 제목 없는 영역)
  items: SidebarMenuItem[];
}

export const COMMON_TOP_SECTION: SidebarSection = {
  items: [
    {
      id: 'dashboard',
      name: '대시보드',
      icon: 'dashboard',
      routeName: 'workspaceDashboard'
    }
  ]
};

export const COMMON_BOTTOM_SECTION: SidebarSection = {
  title: '커뮤니티',
  items: [
    {
      id: 'notice',
      name: '공지사항',
      icon: 'campaign',
      routeName: 'workspaceNotice'
    },
    {
      id: 'qna',
      name: 'Q&A',
      icon: 'help_center',
      routeName: 'workspaceQna'
    }
  ]
};

//수강생(Learner)
export const LEARNER_SECTIONS: SidebarSection[] = [
  {
    title: '학습 관리',
    items: [
      {
        id: 'my-courses',
        name: '내 강의실',
        icon: 'school',
        routeName: 'workspaceLearnerCourses'
      },
      {
        id: 'stats',
        name: '학습 통계',
        icon: 'analytics',
        routeName: 'workspaceLearnerStats'
      }
    ]
  }
];

//강사(trainer)
export const TRAINER_SECTIONS: SidebarSection[] = [
  {
    title: '학습 관리',
    items: [
      {
        id: 'courses',
        name: '교육과정',
        icon: 'library_books',
        routeName: 'workspaceTrainerCourses'
      },
      {
        id: 'stats',
        name: '학습 통계',
        icon: 'analytics',
        routeName: 'workspaceTrainerStats'
      }
    ]
  }
];

//관리자(admin)
export const ADMIN_SECTIONS: SidebarSection[] = [
  {
    title: '워크스페이스 관리',
    items: [
      {
        id: 'settings',
        name: '워크스페이스 설정',
        icon: 'settings',
        routeName: 'workspaceSettings'
      },
      {
        id: 'members',
        name: '참여진 관리',
        icon: 'group',
        routeName: 'workspaceMembers'
      },
      {
        id: 'applications',
        name: '가입신청 관리',
        icon: 'how_to_reg',
        routeName: 'workspaceApplications'
      },
      {
        id: 'invite',
        name: '초대코드 센터',
        icon: 'mail',
        routeName: 'workspaceInvites'
      }
    ]
  }
];

export const ROLE_SECTIONS: Record<Role, SidebarSection[]> = {
  LEARNER: LEARNER_SECTIONS,
  TRAINER: TRAINER_SECTIONS,
  ADMIN: ADMIN_SECTIONS
};

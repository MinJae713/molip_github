import { createRouter, createWebHistory } from "vue-router";
import { useWorkspaceStore } from "@/stores/workspace"; // 스토어 임포트

//기본 레이아웃 설정
import MainLayout from "@/layouts/MainLayout.vue";
import OnlyNavbarLayout from "@/layouts/OnlyNavbarLayout.vue";
import MypageLayout from "@/layouts/MypageLayout.vue";

//테스트용
import TestView from "@/views/Common/TestView.vue";

// 공통 라우트
import WorkspacesView from "@/views/Common/WorkspacesView.vue";
import MypageView from "@/views/Common/MypageView.vue";

// 강사 라우트
import TrainerWorkspace from "@/views/Trainer/TrainerWorkspace.vue";
// 관리자 라우트
import AdminWorkspaceView from "@/views/Admin/AdminWorkspaceView.vue";
import WorkspaceSettingsView from "@/views/Admin/WorkspaceSettingsView.vue";
import MemberManagementView from "@/views/Admin/MemberManagementView.vue";
import JoinRequestView from "@/views/Admin/JoinRequestView.vue";
// 학습자 라우트
import LearnerWorkspace from "@/views/Learner/LearnerWorkspace.vue";
import LearnerMyCoursesView from "@/views/Learner/LearnerMyCoursesView.vue";
import InviteCodeView from "@/views/Admin/InviteCodeView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      component: MainLayout,
      // 3. 자식 라우트: MainLayout 내부의 <RouterView />에 렌더링될 페이지들입니다.
      children: [
        {
          path: "",
          name: "test",
          component: TestView,
        },
      ],
    },
    {
      path: "/workspaces",
      component: OnlyNavbarLayout,
      children: [
        {
          path: "",
          name: "workspaces",
          component: WorkspacesView,
        },
      ],
    },
    {
      path: "/mypage",
      component: MypageLayout,
      children: [
        {
          path: "",
          name: "mypage",
          component: MypageView,
        },
      ],
    },
    {
      path: "/workspace/:id/learner",
      component: MainLayout,
      children: [
        {
          path: "",
          name: "workspaceLearner",
          component: LearnerWorkspace,
        },
        {
          path: "courses",
          name: "workspaceLearnerCourses",
          component: LearnerMyCoursesView,
        },
      ],
    },
    {
      path: "/workspace/:id/admin",
      component: MainLayout,
      children: [
        {
          path: "",
          name: "workspaceAdmin",
          component: AdminWorkspaceView,
        },
        {
          path: 'settings',
          name: 'AdminWorkspaceSettings',
          component: WorkspaceSettingsView,
        },
        {
          path: 'members',
          name: 'MemberManagement',
          component: MemberManagementView
        },
        {
          path: 'requests',
          name: 'JoinRequest',
          component: JoinRequestView
        },
        {
          path: 'invite-codes',
          name: 'InviteCodeCenter',
          component: InviteCodeView
        }
      ],
    },
    {
      path: "/workspace/:id/trainer",
      component: MainLayout,
      children: [
        {
          path: "",
          name: "workspaceTrainer",
          component: TrainerWorkspace,
        },
      ],
    },
    // 하위 호환성을 위한 기존 라우트 (리다이렉트)
    {
      path: "/workspaceDetail/:id",
      redirect: (to) => {
        // 기본적으로 learner로 리다이렉트
        return { name: "workspaceLearner", params: { id: to.params.id } };
      },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  // workspace 페이지 (learner 또는 trainer)로 진입할 때만 로직 실행
  const isWorkspaceRoute =
    to.name === "workspaceLearner" ||
    to.name === "workspaceTrainer" ||
    to.name === "workspaceLearnerCourses" ||
    to.name === "workspaceTrainerCourses";

  if (isWorkspaceRoute) {
    const workspaceStore = useWorkspaceStore();
    const targetId = to.params.id as string;

    // 목록이 비어있다면(새로고침 등) 먼저 데이터를 불러옴
    if (workspaceStore.myWorkspaces.length === 0) {
      await workspaceStore.fetchMyWorkspaces();
    }

    // 권한 체크 (스토어에 만든 isAccessible 또는 find 로직 활용)
    const hasAccess = workspaceStore.isAccessible(targetId);
    if (!hasAccess) {
      alert("해당 워크스페이스에 대한 접근 권한이 없습니다.");
      return next({ name: "mypage" }); // 권한 없으면 마이페이지로 리다이렉트
    }
    // 권한이 있다면 현재 워크스페이스 정보 동기화 후 진입
    await workspaceStore.setCurrentWorkspaceById(targetId);
  }

  next(); // 그 외의 페이지는 정상 진입
});

export default router;

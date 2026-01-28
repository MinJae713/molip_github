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
import LoginView from "@/views/Common/LoginView.vue";

// 강사 라우트
import TrainerWorkspace from "@/views/Trainer/TrainerWorkspace.vue";
import TrainerMyCoursesView from "@/views/Trainer/TrainerMyCoursesView.vue";
import TrainerLiveSessionView from "@/views/Trainer/TrainerLiveSessionView.vue";
// 관리자 라우트
import AdminWorkspaceView from "@/views/Admin/AdminWorkspaceView.vue";
import AdminWorkspaceSettingsView from "@/views/Admin/AdminWorkspaceSettingsView.vue";
import AdminMemberManagementView from "@/views/Admin/AdminMemberManagementView.vue";
import AdminJoinRequestView from "@/views/Admin/AdminJoinRequestView.vue";
import AdminInviteCenterView from "@/views/Admin/AdminInviteCenterView.vue";
// 학습자 라우트
import LearnerMyCoursesView from "@/views/Learner/LearnerMyCoursesView.vue";
import LearnerClassroomView from "@/views/Learner/LearnerClassroomView.vue";
import LearnerLiveSessionView from "@/views/Learner/LearnerLiveSessionView.vue";
import LearnerWorkspace from "@/views/Learner/LearnerWorkspace.vue";
import LearnerStatisticsView from "@/views/Learner/LearnerStatisticsView.vue";
import LearnerReviewView from "@/views/Learner/LearnerReviewView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      component: OnlyNavbarLayout,
      children: [
        {
          path: "",
          name: "loginPage",
          component: LoginView,
          meta: { guestOnly: true },
        },
      ],
    },
    {
      path: "/login",
      name: "login",
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: "/workspaces",
      component: OnlyNavbarLayout,
      children: [
        {
          path: "",
          name: "workspaces",
          component: WorkspacesView,
          meta: { requiresAuth: true },
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
          meta: { requiresAuth: true },
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
          meta: { requiresAuth: true },
        },
        {
          path: "courses",
          name: "workspaceLearnerCourses",
          component: LearnerMyCoursesView,
        },
        {
          path: "classroom/:courseId",
          name: "workspaceLearnerClassroom",
          component: LearnerClassroomView,
        },
        {
          path: "my-stats",
          name: "workspaceLearnerMyStats",
          component: LearnerStatisticsView,
        },
      ],
    },
    {
      path: "/workspace/:id/admin",
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: "",
          name: "workspaceAdmin",
          component: AdminWorkspaceView,
        },
        {
          path: 'settings',
          name: 'workspaceAdminSettings',
          component: AdminWorkspaceSettingsView,
        },
        {
          path: 'members',
          name: 'workspaceAdminMembers',
          component: AdminMemberManagementView,
        },
        {
          path: 'requests',
          name: 'workspaceAdminRequest',
          component: AdminJoinRequestView,
        },
        {
          path: 'invite',
          name: 'workspaceAdminInvite',
          component: AdminInviteCenterView,
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
          meta: { requiresAuth: true },
        },
        {
          path: "courses",
          name: "workspaceTrainerCourses",
          component: TrainerMyCoursesView,
        },
      ],
    },
    // 기존 라우트
    {
      path: "/workspaceDetail/:id",
      redirect: (to) => {
        // 기본적으로 learner로 리다이렉트
        return { name: "workspaceLearner", params: { id: to.params.id } };
      },
    },
    {
      path: "/live-session/:sessionId",
      name: "learnerLiveSession",
      component: LearnerLiveSessionView,
      meta: { requiresAuth: true },
    },
    {
      path: '/live',
      name: 'trainerLiveSession',
      component: () => import('@/views/Trainer/TrainerLiveSessionView.vue')
    }
    {
      path: "/review/:sessionId",
      name: "learnerReview",
      component: LearnerReviewView,
      meta: { requiresAuth: true },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem("accessToken");

  // 1. 비로그인 접근 제어 (requiresAuth)
  // meta.requiresAuth가 true이거나, 명시되지 않은 페이지도 기본적으로 보호하고 싶다면 조건 수정
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!token) {
      // alert("로그인이 필요합니다."); // UX 선택사항
      return next({ name: "loginPage" });
    }
  }

  // 2. 로그인된 사용자 접근 제어 (guestOnly)
  if (to.matched.some((record) => record.meta.guestOnly)) {
    if (token) {
      return next({ name: "workspaces" });
    }
  }

  // 3. 기존 워크스페이스 권한 체크 로직
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

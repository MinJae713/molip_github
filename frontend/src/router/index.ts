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
import SignUpView from "@/views/Common/SingUpView.vue";

// 강사 라우트
import TrainerWorkspaceView from "@/views/Trainer/TrainerWorkspaceView.vue";
import TrainerMyCoursesView from "@/views/Trainer/TrainerMyCoursesView.vue";
import TrainerLiveSessionView from "@/views/Trainer/TrainerLiveSessionView.vue";
import TrainerCourseView from "@/views/Trainer/TrainerCourseView.vue";
// 관리자 라우트
import AdminWorkspaceView from "@/views/Admin/AdminWorkspaceView.vue";
import AdminWorkspaceSettingsView from "@/views/Admin/AdminWorkspaceSettingsView.vue";
import AdminMemberManagementView from "@/views/Admin/AdminMemberManagementView.vue";
import AdminJoinRequestView from "@/views/Admin/AdminJoinRequestView.vue";
import AdminInviteCenterView from "@/views/Admin/AdminInviteCenterView.vue";
// 학습자 라우트
import LearnerMyCoursesView from "@/views/Learner/LearnerMyCoursesView.vue";
import LearnerCourseView from "@/views/Learner/LearnerCourseView.vue";
import LearnerLiveSessionView from "@/views/Learner/LearnerLiveSessionView.vue";
import LearnerWorkspace from "@/views/Learner/LearnerWorkspace.vue";
import LearnerStatisticsView from "@/views/Learner/LearnerStatisticsView.vue";
import LearnerReviewView from "@/views/Learner/LearnerReviewView.vue";

import LearnerLiveSessionViewTest from "@/views/Learner/LearnerLiveSessionViewTest.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "landing",
      component: () => import("@/views/Common/LandingPage.vue"),
      meta: { guestOnly: true },
    },
    {
      path: "/login",
      component: () => import("@/layouts/OnlyNavbarLayout.vue"),
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
      path: "/signup",
      name: "signup",
      component: SignUpView,
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
        {
          path: "certificate",
          name: "mypageCertificate",
          component: () => import("@/views/Common/PreparingView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "verification",
          name: "mypageVerification",
          component: () => import("@/views/Common/PreparingView.vue"),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: "/workspace/:id/learner",
      component: MainLayout,
      meta: { requiresAuth: true, requiresRole: "LEARNER" },
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
        {
          path: "courses/:courseId",
          name: "workspaceLearnerCourseDetail",
          component: LearnerCourseView,
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
      meta: { requiresAuth: true, requiresRole: "ADMIN" },
      children: [
        {
          path: "",
          name: "workspaceAdmin",
          component: AdminWorkspaceView,
        },
        {
          path: "settings",
          name: "workspaceAdminSettings",
          component: AdminWorkspaceSettingsView,
        },
        {
          path: "members",
          name: "workspaceAdminMembers",
          component: AdminMemberManagementView,
        },
        {
          path: "requests",
          name: "workspaceAdminRequest",
          component: AdminJoinRequestView,
        },
        {
          path: "invite",
          name: "workspaceAdminInvite",
          component: AdminInviteCenterView,
        },
      ],
    },
    {
      path: "/workspace/:id/trainer",
      component: MainLayout,
      meta: { requiresAuth: true, requiresRole: "TRAINER" },
      children: [
        {
          path: "",
          name: "workspaceTrainer",
          component: TrainerWorkspaceView,
        },
        {
          path: "courses",
          name: "workspaceTrainerCourses",
          component: TrainerMyCoursesView,
        },
        {
          path: "courses/:courseId",
          name: "workspaceTrainerCourseDetail",
          component: TrainerCourseView,
        },
        {
          path: "stats",
          name: "workspaceTrainerStatistics",
          component: () => import("@/views/Trainer/TrainerStatisticsView.vue"),
        },
      ],
    },
    // 기존 라우트
    {
      path: "/workspaceDetail/:id",
      redirect: (to) => {
        // 기본적으로 learner로 리다이렉트 (권한 가드가 이후에 처리)
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
      path: "/live-session/test",
      name: "learnerLiveSessionTest",
      component: LearnerLiveSessionViewTest,
      meta: { requiresAuth: true },
    },
    {
      path: "/trainer/live/:courseId/:sessionId",
      name: "trainerLiveSession",
      component: TrainerLiveSessionView,
      meta: { requiresAuth: true }, // 강사 라이브도 인증 필요
    },
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
  const isAuthenticated = !!token;

  // 1. 인증 체크 (Auth Guard)
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      return next({ name: "loginPage" });
    }
  }

  // 2. 로그인된 상태에서 로그인/회원가입 페이지 접근 차단 (Guest Guard)
  if (to.matched.some((record) => record.meta.guestOnly)) {
    if (isAuthenticated) {
      return next({ name: "workspaces" });
    }
  }

  // 3. 워크스페이스 권한 및 역할 체크 (Workspace & RBAC Guard)
  // URL에 workspaceId가 포함된 경우 ( /workspace/:id/... )
  const workspaceId = to.params.id as string;
  if (workspaceId) {
    const workspaceStore = useWorkspaceStore();

    // 3-1. 워크스페이스 목록 로드 (로드되지 않았을 경우)
    if (!workspaceStore.isLoaded) {
      await workspaceStore.fetchMyWorkspaces(true);
    }

    // 3-2. 해당 워크스페이스 가입 여부 확인 (활동 중인 상태인지)
    const hasAccess = workspaceStore.isAccessible(workspaceId);
    if (!hasAccess) {
      alert(
        "해당 워크스페이스에 대한 접근 권한이 없거나 가입 승인 대기 중입니다.",
      );
      return next({ name: "workspaces" });
    }

    // 3-3. 현재 워크스페이스 정보 동기화 (역할 정보 갱신을 위해)
    await workspaceStore.setCurrentWorkspaceById(workspaceId);

    // 3-4. 역할(Role) 일치 여부 확인
    const requiredRole = to.matched.find((r) => r.meta.requiresRole)?.meta
      .requiresRole as string;
    if (requiredRole) {
      const currentRole = workspaceStore.currentRole; // 'LEARNER', 'TRAINER', 'ADMIN' (DB 대문자 기준)

      if (currentRole !== requiredRole) {
        // 역할이 다르면 본인 역할에 맞는 메인 페이지로 리다이렉트
        alert(
          `접근 권한이 없습니다. (요청: ${requiredRole}, 현재: ${currentRole})`,
        );

        const roleFallback: Record<string, string> = {
          LEARNER: "workspaceLearner",
          TRAINER: "workspaceTrainer",
          ADMIN: "workspaceAdmin",
        };

        const targetName = roleFallback[currentRole || ""] || "workspaces";
        return next({ name: targetName, params: { id: workspaceId } });
      }
    }
  }

  next(); // 모든 검증 통과 시 진입
});

export default router;

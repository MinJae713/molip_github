import { ref, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type {
  LearnerWorkspaceTopCardStats,
  WeeklyStat,
  SessionConcentrationStats,
  LearnerSessionReport,
} from "@/types";

export const useStatsStore = defineStore("stats", () => {
  // 수강생 상단 통계 박스 호출 api
  const learnerWorkspaceTopCardStats = ref<LearnerWorkspaceTopCardStats | null>(
    null,
  );
  // 선택된 코스 ID (전역 상태로 관리 for LearnerStatisticsView)
  const selectedCourseId = ref<string | number>("all");
  async function fetchLearnerWorkspaceTopCardStats(
    workspaceId: string,
    courseId: string | number,
    user_id: string | number,
  ) {
    try {
      // courseId가 'all'이 아니면 해당 courseId로, 'all'이면 'all'로 호출 (API 경로 통일성을 위해)
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/stats/${user_id}`,
      );
      if (response.data && response.data.length > 0) {
        learnerWorkspaceTopCardStats.value = response.data[0];
      } else {
        learnerWorkspaceTopCardStats.value = null;
      }
    } catch (error) {
      console.error("Failed to fetch learner workspace top card stats:", error);
    }
  }
  // 수강생 주간 학습 시간(코스별, 전체)api
  const learnerWeeklyStats = ref<WeeklyStat[]>([]);
  async function fetchLearnerWeeklyStats(
    workspaceId: string,
    courseId: string | number,
    userId: string | number,
  ) {
    try {
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/weekly-stats/${userId}`,
      );
      // json-server 쿼리 응답은 배열이므로 [0] 접근
      if (response.data && response.data.length > 0) {
        learnerWeeklyStats.value = response.data[0].weekly_data;
      } else {
        learnerWeeklyStats.value = [];
      }
    } catch (error) {
      console.error("Failed to fetch weekly stats:", error);
    }
  }
  // 세션별 평균 집중도 상태 & API 액션
  const learnerSessionStats = ref<SessionConcentrationStats | null>(null);
  async function fetchLearnerSessionStats(
    workspaceId: string,
    courseId: string | number,
    userId: string | number,
  ) {
    try {
      // API 경로 /workspaces/:workspace_id/courses/:course_id/session-stats/:user_id
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/session-stats/${userId}`,
      );
      if (response.data && response.data.length > 0) {
        learnerSessionStats.value = response.data[0];
      } else {
        learnerSessionStats.value = null; // 데이터 없을 때 초기화
      }
    } catch (error) {
      console.error("Failed to fetch session stats:", error);
      learnerSessionStats.value = null;
    }
  }
  // 세션별 상세 리포트 상태
  const learnerSessionReports = ref<LearnerSessionReport | null>(null);
  async function fetchLearnerSessionReports(
    workspaceId: string,
    courseId: string | number,
    userId: string | number,
  ) {
    // [수정 포인트] 'all' 선택 시 데이터 비우고 리턴 (호출 X)
    if (courseId === "all") {
      learnerSessionReports.value = null;
      return;
    }
    try {
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/session-reports/${userId}`,
      );
      if (response.data && response.data.length > 0) {
        learnerSessionReports.value = response.data[0];
      } else {
        learnerSessionReports.value = null;
      }
    } catch (error) {
      console.error("Failed to fetch session reports:", error);
      learnerSessionReports.value = null;
    }
  }

  return {
    learnerWorkspaceTopCardStats,
    learnerWeeklyStats,
    learnerSessionStats,
    selectedCourseId, // 추가
    learnerSessionReports,
    fetchLearnerWorkspaceTopCardStats,
    fetchLearnerWeeklyStats,
    fetchLearnerSessionStats,
    fetchLearnerSessionReports,
  };
});

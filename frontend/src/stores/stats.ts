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
  // Data Adapters
  const mapTopCardStats = (data: any): LearnerWorkspaceTopCardStats => ({
    id: data.id,
    userId: data.user_id || data.userId,
    workspaceId: data.workspace_id || data.workspaceId,
    role: data.role,
    values: data.values,
  });

  const mapSessionStats = (data: any): SessionConcentrationStats => ({
    id: data.id,
    userId: data.user_id || data.userId,
    courseId: data.course_id || data.courseId,
    workspaceId: data.workspace_id || data.workspaceId,
    labels: data.labels,
    myScores: data.my_scores || data.myScores,
    avgScores: data.avg_scores || data.avgScores,
    currentFocus: data.current_focus || data.currentFocus,
  });

  const mapSessionReport = (data: any): LearnerSessionReport => ({
    id: data.id,
    userId: data.user_id || data.userId,
    workspaceId: data.workspace_id || data.workspaceId,
    courseId: data.course_id || data.courseId,
    sessions: (data.sessions || []).map((s: any) => ({
      id: s.id,
      name: s.name,
      date: s.date,
      duration: s.duration,
      focus: s.focus,
      participation: s.participation,
      activityScore: s.activity_score || s.activityScore,
    })),
  });

  async function fetchLearnerWorkspaceTopCardStats(
    workspaceId: string,
    courseId: string | number,
    userId: string | number, // Changed user_id to userId match types
  ) {
    try {
      // API call param uses snake_case if backend requires, usually pure path params are fine
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/stats/${userId}`,
      );
      if (response.data && response.data.length > 0) {
        learnerWorkspaceTopCardStats.value = mapTopCardStats(response.data[0]);
      } else {
        learnerWorkspaceTopCardStats.value = null;
      }
    } catch (error) {
      console.error("Failed to fetch learner workspace top card stats:", error);
    }
  }
  // 수강생 주간 학습 시간(코스별, 전체)api
  // This one returns WeeklyStat[] which is simpler
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
      if (response.data && response.data.length > 0) {
        learnerWeeklyStats.value = response.data[0].weekly_data; // Assuming weekly_data structure is already compatible or needs mapping?
        // WeeklyStat uses: label, time, percentage, isHighlight. Typically these are generic.
        // If backend sends snake_case labels, map here too.
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
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/session-stats/${userId}`,
      );
      if (response.data && response.data.length > 0) {
        learnerSessionStats.value = mapSessionStats(response.data[0]);
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
    if (courseId === "all") {
      learnerSessionReports.value = null;
      return;
    }
    try {
      const response = await api.get(
        `workspaces/${workspaceId}/courses/${courseId}/session-reports/${userId}`,
      );
      if (response.data && response.data.length > 0) {
        learnerSessionReports.value = mapSessionReport(response.data[0]);
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

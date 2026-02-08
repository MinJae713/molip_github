import { ref, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type {
  LearnerWorkspaceTopCardStats,
  WeeklyStat,
  SessionConcentrationStats,
  LearnerSessionReport,
  CourseStatsResponse,
} from "@/types";

export const useStatsStore = defineStore("stats", () => {
  // 수강생 상단 통계 박스 호출 api
  const learnerWorkspaceTopCardStats = ref<LearnerWorkspaceTopCardStats | null>(
    null,
  );

  // 전체 통계 데이터 (API 원본)
  const courseStats = ref<CourseStatsResponse | null>(null);

  // Derived states (formerly fetched individually)
  const learnerWeeklyStats = ref<WeeklyStat[]>([]);
  const learnerSessionStats = ref<SessionConcentrationStats | null>(null);
  const learnerSessionReports = ref<LearnerSessionReport | null>(null);

  // 선택된 코스 ID (전역 상태로 관리 for LearnerStatisticsView)
  const selectedCourseId = ref<string | number>("all");
  // Data Adapters
  const mapTopCardStats = (
    data: CourseStatsResponse,
  ): LearnerWorkspaceTopCardStats => {
    // Format Learning Time (Minutes -> X시간 Y분)
    const totalMinutes = data.totalLearningTime;
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    const formattedTime = `${hours}시간 ${minutes}분`;

    return {
      id: data.courseId,
      userId: 0,
      workspaceId: 0,
      role: "LEARNER",
      values: [
        {
          icon: "timer", // Changed to timer
          title: "총 학습 시간", // Matches wireframe text "이번 주 학습 시간" (though it's course total really)
          unit: "", // Unit included in value
          value: formattedTime,
          iconBgClass: "bg-indigo-50",
          iconColorClass: "text-indigo-600",
        },
        {
          icon: "assignment_turned_in", // Checkboard icon
          title: "퇴실 퀴즈 평균",
          unit: "점",
          value: data.exitQuizAverage,
          iconBgClass: "bg-emerald-50",
          iconColorClass: "text-emerald-600",
        },
        {
          icon: "psychology", // Head icon
          title: "평균 집중도",
          unit: "%",
          value: data.concentrateMyAverage,
          iconBgClass: "bg-purple-50", // Lighter purple for background
          iconColorClass: "text-purple-600",
        },
      ],
    };
  };

  const mapSessionStats = (
    data: CourseStatsResponse,
  ): SessionConcentrationStats => ({
    id: data.courseId,
    userId: 0,
    courseId: data.courseId,
    workspaceId: 0,
    labels: data.sessionRecords.map((s) => s.title), // or s.title
    myScores: data.sessionRecords.map((s) => s.concentrate),
    avgScores: data.sessionRecords.map((s) => s.concentrateTotalAverage),
    currentFocus: 0, // Not used in chart directly
  });

  const mapSessionReport = (
    data: CourseStatsResponse,
  ): LearnerSessionReport => ({
    id: data.courseId,
    userId: 0,
    workspaceId: 0,
    courseId: String(data.courseId),
    sessions: data.sessionRecords.map(
      (s) =>
        ({
          id: s.sessionId,
          name: s.title,
          date: s.date,
          duration: `${s.learningTime}분`,
          focus: s.concentrate,
          participation: s.isAttendance ? "출석" : "결석",
          activityScore: s.exitQuiz, // using exitQuiz as activity score/quiz score
          isProceed: s.isProceed,
        }) as any,
    ), // casting to any to allow new fields if type isn't fully updated yet, or we update LearnerSessionReport type
  });

  // WeeklyStat is now SessionLearningTimeStat
  const mapWeeklyStats = (data: CourseStatsResponse): WeeklyStat[] => {
    // Map session records to bar chart data
    // label: session title, time: learningTime
    return data.sessionRecords.map((s) => ({
      label: s.title,
      time: s.learningTime,
      percentage: 0, // Calculated in component or here? Component handles opacity based on max.
      isHighlight: false,
    }));
  };

  // ==========================================
  // [MOCK DATA] 개발 확인용 목업 데이터 - 나중에 삭제하거나 false로 변경
  // ==========================================
  const USE_MOCK_DATA = false;

  const MOCK_DATA: CourseStatsResponse = {
    courseId: 2,
    totalLearningTime: 275, // 4시간 35분
    exitQuizAverage: 86,
    concentrateMyAverage: 78,
    sessionRecords: [
      {
        sessionId: 6,
        learningTime: 90,
        concentrate: 85,
        concentrateTotalAverage: 71,
        title: "1주차: 생성형 AI의 이해",
        date: "2025-02-01",
        isAttendance: true,
        exitQuiz: 85,
        isProceed: true,
      },
      {
        sessionId: 7,
        learningTime: 90,
        concentrate: 92,
        concentrateTotalAverage: 75,
        title: "2주차: 프롬프트 엔지니어링 기초",
        date: "2025-02-07",
        isAttendance: true,
        exitQuiz: 90,
        isProceed: true,
      },
      {
        sessionId: 8,
        learningTime: 95,
        concentrate: 65,
        concentrateTotalAverage: 64,
        title: "3주차: LLM 활용 실습",
        date: "2025-02-14",
        isAttendance: true,
        exitQuiz: 80,
        isProceed: true,
      },
      {
        sessionId: 9,
        learningTime: 0,
        concentrate: 0,
        concentrateTotalAverage: 80,
        title: "4주차: RAG 아키텍처",
        date: "2025-02-21",
        isAttendance: false,
        exitQuiz: 0,
        isProceed: false,
      },
      {
        sessionId: 10,
        learningTime: 0,
        concentrate: 0,
        concentrateTotalAverage: 90,
        title: "5주차: 프로젝트 기획",
        date: "2025-02-28",
        isAttendance: false,
        exitQuiz: 0,
        isProceed: false,
      },
    ],
  };

  async function fetchCourseStats(courseId: string | number) {
    if (!courseId || courseId === "all") {
      // Clear data or handle 'all' case if backend supports it
      learnerWorkspaceTopCardStats.value = null;
      learnerWeeklyStats.value = [];
      learnerSessionStats.value = null;
      learnerSessionReports.value = null;
      return;
    }

    // [MOCK] 목업 데이터 적용
    if (USE_MOCK_DATA) {
      setTimeout(() => {
        const data = MOCK_DATA;
        courseStats.value = data;
        learnerWorkspaceTopCardStats.value = mapTopCardStats(data);
        learnerWeeklyStats.value = mapWeeklyStats(data);
        learnerSessionStats.value = mapSessionStats(data);
        learnerSessionReports.value = mapSessionReport(data);
      }, 500); // 로딩 느낌을 위한 딜레이
      return;
    }
    // 진짜 데이터
    try {
      console.log("Fetching course stats for courseId:", courseId);
      const response = await api.get(`/courses/${courseId}/stats`);
      const data = response.data as CourseStatsResponse;
      courseStats.value = data;
      console.log("Course Stats:", data);

      // Update all derived states
      learnerWorkspaceTopCardStats.value = mapTopCardStats(data);
      learnerWeeklyStats.value = mapWeeklyStats(data);
      learnerSessionStats.value = mapSessionStats(data);
      learnerSessionReports.value = mapSessionReport(data);
    } catch (error) {
      console.error("Failed to fetch course stats:", error);
      // Reset on error
      learnerWorkspaceTopCardStats.value = null;
      learnerWeeklyStats.value = [];
      learnerSessionStats.value = null;
      learnerSessionReports.value = null;
    }
  }

  // Deprecated/Unused individual fetchers can be removed or kept as stubs if used elsewhere
  async function fetchLearnerWorkspaceTopCardStats() {}
  async function fetchLearnerWeeklyStats() {}
  async function fetchLearnerSessionStats() {}
  async function fetchLearnerSessionReports() {}

  return {
    learnerWorkspaceTopCardStats,
    learnerWeeklyStats,
    learnerSessionStats,
    selectedCourseId, // 추가
    learnerSessionReports,
    fetchCourseStats,
    // fetchLearnerWorkspaceTopCardStats, // Removing from export to force usage of fetchCourseStats
    // fetchLearnerWeeklyStats,
    // fetchLearnerSessionStats,
    // fetchLearnerSessionReports,
  };
});

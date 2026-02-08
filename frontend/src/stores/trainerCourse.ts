import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import type { LearnerCourse, TrainerCourse, CreateCourse } from "@/types";

// 통계 데이터 타입 정의
interface CourseStatistics {
  avgAttandance: number;
  avgConcentrate: number;
  avgQuizCorrect: number;
  courseId: number;
  isCertificated: boolean;
  courseLearnerAnalysis: {
    memberId: number;
    name: string;
    email: string;
    isCertificated: boolean;
    workspaceMemberId: number;
    courseLearnerId: number;
    sumConcentrate: number;
    sumAttendance: number;
    sumQuizCorrect: number;
  }[];
  sessionAnalysis: {
    sessionId: number;
    sessionName?: string;
    title?: string;
    avgAttandance: number;
    avgConcentrate: number;
    avgQuizCorrect: number;
  }[];
}

export const useTrainerCourseStore = defineStore("trainerCourse", () => {
  const workspaceAllCourses = ref<TrainerCourse[]>([]); // 현재 워크스페이스의 모든 코스 목록
  const managedCourses = ref<any[]>([]); // 강사가 관리하는 전체 코스 목록
  const currentManagedCourse = ref<TrainerCourse | null>(null); // 현재 선택/관리 중인 코스 상세
  const studentRequests = ref<any[]>([]); // 수강 신청 목록
  const students = ref<any[]>([]); // 수강생 목록
  const isLoading = ref(false);

  // Helper: API -> Frontend (CamelCase)
  const mapTrainerCourse = (data: any): TrainerCourse => ({
    courseId: data.course_id || data.courseId || data.id,
    title: data.title,
    learnerCount: data.learnerCount || 0,
    totalSessionCount: data.totalSessionCount || 0,
    averageProgressRate:
      data.averageProgressRate !== undefined ? data.averageProgressRate : null,
    courseStatus: data.courseStatus || data.status || "PROCEED",
    // 기존 필드들 (필요시 선택적 유지)
    category: data.category,
    description: data.description,
    status: data.status,
  });

  // 강사 코스 상세 통계 조회 (전체 분석)
  const courseStatistics = ref<CourseStatistics | null>(null);
  async function fetchCourseStats(courseId: string | number) {
    try {
      isLoading.value = true;
      const response = await api.get(`/courses/${courseId}/stats`);
      courseStatistics.value = response.data;
    } catch (error) {
      console.error("코스 통계 조회 실패:", error);
      courseStatistics.value = null;
    } finally {
      isLoading.value = false;
    }
  }

  // 코스 정보 가져오기
  async function fetchCourseInfo(courseId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${courseId}`);
      currentManagedCourse.value = mapTrainerCourse(response.data);
    } catch (error) {
      console.error("코스 상세 정보 로드 실패:", error);
      currentManagedCourse.value = null;
    } finally {
      isLoading.value = false;
    }
  }

  // 워크스페이스의 모든 코스 목록 가져오기(강사)
  async function fetchWorkspaceAllCourses(workspaceId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);
      workspaceAllCourses.value = response.data;
    } catch (error) {
      console.error("워크스페이스 모든 코스 목록 로드 실패:", error);
      workspaceAllCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // 내 코스 가져오기
  async function fetchMyCourses(workspaceId: string | number) {
    isLoading.value = true;
    console.log(`[API Request] workspaceId: ${workspaceId}`);
    try {
      const response = await api.get(`/courses/${workspaceId}/trainer`);

      console.log("[API Success] 응답 데이터:", response.data);
      managedCourses.value = Array.isArray(response.data) ? response.data : [];
    } catch (error: any) {
      // 상세 에러 로깅
      if (error.response) {
        console.error(
          `[API Error ${error.response.status}]`,
          error.response.data,
        );
      } else {
        console.error("[API Error] 네트워크 혹은 설정 오류:", error.message);
      }
      managedCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // 특정 코스 관리 모드 진입 (상세 정보 로드)
  async function setManagedCourse(courseId: string | number) {
    isLoading.value = true;
    try {
      // 이미 목록에 있다면 재활용
      const found = managedCourses.value.find(
        (c) => String(c.courseId) === String(courseId),
      );
      if (found) {
        currentManagedCourse.value = found;
      } else {
        const response = await api.get(`/courses/${courseId}`);
        currentManagedCourse.value = mapTrainerCourse(response.data);
      }
    } catch (error) {
      console.error("코스 상세 정보 로드 실패:", error);
      currentManagedCourse.value = null;
    } finally {
      isLoading.value = false;
    }
  }

  // 새 코스 생성
  async function createCourse(
    payload: Partial<CreateCourse>,
    workspace_id: string | number,
  ) {
    isLoading.value = true;
    try {
      console.log(payload);
      // post 일 때 워크스페이스 생성
      await api.post("/courses", payload);
      // 목록 갱신
      await fetchMyCourses(workspace_id);
      return true;
    } catch (error) {
      console.error("코스 생성 실패:", error);
      return false;
    } finally {
      isLoading.value = false;
    }
  }
  // 강의 세션 목록 조회
  const fetchCourseSessions = async (courseId: string | number) => {
    try {
      const response = await api.get(`/session?courseId=${courseId}`);
      return response.data;
    } catch (error) {
      console.error("세션 목록 로드 실패:", error);
      return [];
    }
  };

  const fetchStudentRequests = async (courseId: string | number) => {
    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${courseId}/applies`);
      studentRequests.value = response.data;
    } catch (error) {
      console.error("수강 신청 목록 로드 실패:", error);
      studentRequests.value = [];
    } finally {
      isLoading.value = false;
    }
  };

  const approveStudent = async (
    courseId: string | number,
    workspaceMemberId: number,
  ) => {
    try {
      await api.post(`/courses/${courseId}/approve`, {
        workspaceMemberId,
        isApply: true,
      });
      console.log(
        `[API Success] approveStudent ${workspaceMemberId} in course ${courseId}`,
      );

      // 목록에서 제거
      studentRequests.value = studentRequests.value.filter(
        (s) => s.workspaceMemberId !== workspaceMemberId,
      );
      // 수강생 명단 갱신
      await fetchStudents(courseId);
    } catch (error) {
      console.error("수강생 승인 실패:", error);
    }
  };

  const rejectStudent = async (
    courseId: string | number,
    workspaceMemberId: number,
  ) => {
    try {
      await api.post(`/courses/${courseId}/approve`, {
        workspaceMemberId,
        isApply: false,
      });
      console.log(
        `[API Success] rejectStudent ${workspaceMemberId} in course ${courseId}`,
      );

      studentRequests.value = studentRequests.value.filter(
        (s) => s.workspaceMemberId !== workspaceMemberId,
      );
    } catch (error) {
      console.error("수강생 거절 실패:", error);
    }
  };
  // 지원한 학생 조회

  const fetchApplyStudents = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}/applies`);
      return response.data;
    } catch (error) {
      console.error("수강 신청 목록 로드 실패:", error);
      return [];
    }
  };

  const fetchStudents = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}/learners`);
      students.value = response.data;
    } catch (error) {
      console.error("수강생 목록 로드 실패:", error);
      students.value = [];
    }
  };

  // 세션 삭제
  const deleteSession = async (sessionId: string | number) => {
    try {
      await api.delete(`/session/${sessionId}`);
      return true;
    } catch (error) {
      console.error("세션 삭제 실패:", error);
      return false;
    }
  };

  return {
    workspaceAllCourses,
    managedCourses,
    currentManagedCourse,
    isLoading,
    fetchWorkspaceAllCourses,
    fetchMyCourses,
    setManagedCourse,
    createCourse,
    fetchCourseSessions,
    studentRequests,
    students,
    fetchStudentRequests,
    approveStudent,
    rejectStudent,
    fetchStudents,
    fetchCourseInfo,
    fetchCourseStats,
    courseStatistics,
    fetchApplyStudents,
    deleteSession,
  };
});

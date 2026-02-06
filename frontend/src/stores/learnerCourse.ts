import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type {
  LearnerCourse,
  Material,
  ZzimCourse,
  WorkspaceStats,
} from "@/types";

// 이 스토어는 수강생(Learner)이 수강 중인 코스를 관리하는 역할에 집중
// 강사가 관리하는 코스 정보는 `stores/trainerCourse.ts`를 사용

export const useLearnerCourseStore = defineStore("learnerCourse", () => {
  // --- 상태 (State) ---
  const myEnrolledCourses = ref<LearnerCourse[]>([]); // 내가 수강 신청한 강의
  const workspaceCourses = ref<LearnerCourse[]>([]); // 현재 워크스페이스의 전체 공개 강의 (수강신청용 목록)
  const myLikedCourses = ref<ZzimCourse[]>([]); // 찜한 강의
  const courseInfo = ref<LearnerCourse | null>(null); // (수강생 뷰) 현재 접속한 강의 상세 정보
  const courseMaterials = ref<Material[]>([]); // (수강생 뷰) 강의 자료 목록
  const isLoading = ref(false);
  const myCoursesAttendanceRate = ref<number | null>(null);
  const workspaceStats = ref<WorkspaceStats | null>(null);

  // 캐싱용
  const materialsCache = ref<Map<string | number, Material[]>>(new Map());
  const lastWorkspaceId = ref<string | number | null>(null);

  // 워크스페이스 내 개설된 전체 강의 목록 (수강 신청 가능 목록)
  async function fetchCoursesByWorkspace(
    workspaceId: string | number,
    forceRefresh = false,
  ) {
    if (!workspaceId) return;

    if (
      !forceRefresh &&
      lastWorkspaceId.value === workspaceId &&
      workspaceCourses.value.length > 0
    ) {
      return;
    }

    isLoading.value = true;
    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);
      const allCourses = response.data;
      workspaceCourses.value = allCourses;
      lastWorkspaceId.value = workspaceId;
    } catch (error) {
      console.error("[Learner] 워크스페이스 강의 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 내가 수강 중인 강의 목록
  async function fetchMyEnrolledCourses(workspaceId: string | number) {
    if (!workspaceId) return;

    isLoading.value = true;
    try {
      const response = await api.get(`/courses/me`, {
        params: { workspaceId },
      });
      myEnrolledCourses.value = Array.isArray(response.data)
        ? response.data
        : [];
      console.log("[Learner] 내 수강 강의 로드 성공:", myEnrolledCourses.value);
    } catch (error) {
      console.error("[Learner] 내 수강 강의 로드 실패:", error);
      myEnrolledCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 찜한 강의 목록
  async function fetchLikeCourses(workspaceId: string | number) {
    if (!workspaceId) return;
    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${workspaceId}/zzim`);
      myLikedCourses.value = response.data;
      console.log("[Learner] 찜 목록 로드 성공:", myLikedCourses.value);
    } catch (error) {
      console.error("[Learner] 찜 목록 로드 실패:", error);
      myLikedCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 수료한 강의 목록
  const myCertificatedCourses = ref<any[]>([]); // 타입 호환을 위해 any 또는 별도 인터페이스 사용
  async function fetchMyCertificatedCourses(workspaceId: string | number) {
    if (!workspaceId) return;
    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${workspaceId}/certificated`);
      myCertificatedCourses.value = response.data;
      console.log(
        "[Learner] 수료 강의 로드 성공:",
        myCertificatedCourses.value,
      );
    } catch (error) {
      console.error("[Learner] 수료 강의 로드 실패:", error);
      myCertificatedCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // [U] 강의 찜하기 토글
  // [수정] 성공 여부를 반환하도록(boolean) 반환 타입 수정
  async function toggleCourseLike(
    workspaceId: string | number,
    courseId: string | number,
    isCurrentlyLiked: boolean,
  ): Promise<boolean> {
    try {
      const response = await api.post(`/courses/${workspaceId}/zzim`, {
        courseId: Number(courseId),
        isZzim: !isCurrentlyLiked,
      });

      if (response.data.apiResult) {
        // 성공 시 찜 목록 갱신 시도 (이미 카드에서 부르고 있지만 방어적으로 유지)
        await fetchLikeCourses(workspaceId);

        const updateList = (list: any[]) => {
          const item = list.find(
            (c) => String(c.courseId) === String(courseId),
          );
          if (item) item.isLiked = !isCurrentlyLiked;
        };

        updateList(workspaceCourses.value);
        updateList(myEnrolledCourses.value);

        console.log(`[Learner] 찜 ${!isCurrentlyLiked ? "등록" : "해제"} 성공`);
        return true; // ★ 성공 반환
      }
      return false; // apiResult가 false인 경우
    } catch (error) {
      console.error("찜 처리 중 에러 발생:", error);
      alert("찜 처리에 실패했습니다. 다시 시도해 주세요.");
      return false; // ★ 에러 시 실패 반환
    }
  }

  // 강의 수강 신청
  async function enrollCourse(
    courseId: string | number,
    workspaceId: string | number,
  ) {
    isLoading.value = true;
    try {
      const response = await api.post(`/courses/${courseId}/apply`);
      console.log("[Learner] 수강 신청 완료:", response.data);

      if (workspaceId) {
        await fetchMyEnrolledCourses(workspaceId);
        await fetchCoursesByWorkspace(workspaceId, true);
        await fetchLikeCourses(workspaceId); // 찜 목록 갱신 (상태 변경 반영)
      }
      return { success: true, status: "ok" };
    } catch (error: any) {
      console.error("[Learner] 수강 신청 실패:", error);
      // 이미 신청된 경우 (백엔드 에러 메시지나 코드로 식별)
      // 여기서는 에러 메시지에 '이미' 또는 409 상태코드를 체크한다고 가정
      if (
        error.response?.status === 409 ||
        error.response?.data?.message?.includes("이미")
      ) {
        return {
          success: false,
          status: "already_applied",
          message: error.response?.data?.message,
        };
      }
      return {
        success: false,
        status: "error",
        message: "수강 신청 중 오류가 발생했습니다.",
      };
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 내 출석률 조회
  const fetchMyCoursesAttendanceRate = async (
    year?: number,
    month?: number,
  ) => {
    try {
      const targetYear = year || new Date().getFullYear();
      const targetMonth = month || new Date().getMonth() + 1;
      const response = await api.get(
        `/members/me/courses/attendance-rate?year=${targetYear}&month=${targetMonth}`,
      );
      const data = Array.isArray(response.data)
        ? response.data[0]
        : response.data;
      if (data) {
        myCoursesAttendanceRate.value = data.attendance_rate;
      }
      return data;
    } catch (error) {
      console.error("출석률 로드 실패:", error);
      return null;
    }
  };

  // [R] 단일 강의 상세 정보 (수강생 뷰)
  const fetchCourseInfo = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}`);
      const data = response.data;
      courseInfo.value = data || null;

      // [추가] 상세 정보(진도율 등 포함)를 수강 목록 스토어에도 반영하여 캐싱 효과
      if (data && myEnrolledCourses.value.length > 0) {
        const index = myEnrolledCourses.value.findIndex(c => String(c.courseId) === String(courseId));
        if (index !== -1) {
          // 기존 데이터에 상세 정보를 덮어씀 (currentLessons, totalLessons 등)
          myEnrolledCourses.value[index] = { ...myEnrolledCourses.value[index], ...data };
        }
      }

      return data || null;
    } catch (error) {
      console.error("강의 상세 정보 로드 실패:", error);
      courseInfo.value = null;
      return null;
    }
  };

  // [R] 강의 세션 목록 조회
  const fetchCourseSessions = async (courseId: string | number) => {
    try {
      const response = await api.get(`/session?courseId=${courseId}`);
      return response.data;
    } catch (error) {
      console.error("세션 목록 로드 실패:", error);
      return [];
    }
  };

  // [R] 강의 자료 조회
  async function fetchCourseMaterials(
    courseId: string | number,
    forceRefresh = false,
  ) {
    if (!forceRefresh && materialsCache.value.has(courseId)) {
      courseMaterials.value = materialsCache.value.get(courseId) || [];
      return;
    }

    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${courseId}/materials`);
      materialsCache.value.set(courseId, response.data);
      courseMaterials.value = response.data;
    } catch (error) {
      console.error("강의 자료 로드 실패:", error);
      courseMaterials.value = [];
    } finally {
      isLoading.value = false;
    }
  }
  return {
    myEnrolledCourses,
    workspaceCourses,
    myLikedCourses,
    isLoading,
    myCoursesAttendanceRate,
    courseInfo,
    courseMaterials,
    workspaceStats,
    fetchMyEnrolledCourses,
    fetchCoursesByWorkspace,
    toggleCourseLike,
    enrollCourse,
    fetchCourseSessions,
    fetchMyCoursesAttendanceRate,
    fetchLikeCourses,
    fetchCourseInfo,
    fetchCourseMaterials,
    myCertificatedCourses,
    fetchMyCertificatedCourses,
  };
});

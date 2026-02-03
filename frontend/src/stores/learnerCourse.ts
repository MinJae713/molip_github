import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type { LearnerCourse, Material } from "@/types";

// 이 스토어는 수강생(Learner)이 수강 중인 코스를 관리하는 역할에 집중
// 강사가 관리하는 코스 정보는 `stores/trainerCourse.ts`를 사용

export const useLearnerCourseStore = defineStore("learnerCourse", () => {
  // --- 상태 (State) ---
  const myEnrolledCourses = ref<LearnerCourse[]>([]); // 내가 수강 신청한 강의
  const workspaceCourses = ref<LearnerCourse[]>([]); // 현재 워크스페이스의 전체 공개 강의 (수강신청용 목록)
  const myLikedCourses = ref<LearnerCourse[]>([]); // 찜한 강의
  const courseInfo = ref<LearnerCourse | null>(null); // (수강생 뷰) 현재 접속한 강의 상세 정보
  const courseMaterials = ref<Material[]>([]); // (수강생 뷰) 강의 자료 목록
  const isLoading = ref(false);
  const myCoursesAttendanceRate = ref<number | null>(null);

  // 캐싱용
  const materialsCache = ref<Map<string | number, Material[]>>(new Map());
  const lastWorkspaceId = ref<string | number | null>(null);
  // 워크스페이스 내 개설된 전체 강의 목록 (수강 신청 가능 목록)
  async function fetchCoursesByWorkspace(
    workspaceId: string | number,
    forceRefresh = false,
  ) {
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
      // 워크스페이스 ID로 필터링 (Backend에서 해주면 좋음)
      workspaceCourses.value = allCourses;
      lastWorkspaceId.value = workspaceId;
    } catch (error) {
      console.error("[Learner] 워크스페이스 강의 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 내가 수강 중인 강의 목록
  async function fetchMyEnrolledCourses(
    workspaceId?: string | number,
    userId?: string | number,
    role = "LEARNER",
  ) {
    isLoading.value = true;
    try {
      let response;
      if (workspaceId) {
        response = await api.get(`/courses/me`);
        myEnrolledCourses.value = response.data;
      }
    } catch (error) {
      console.error("[Learner] 내 수강 강의 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 찜한 강의 목록
  async function fetchLikeCourses() {
    isLoading.value = true;
    try {
      const response = await api.get("/members/me/course-likes");
      myLikedCourses.value = response.data;
    } catch (error) {
      console.error("[Learner] 찜한 강의 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // [U] 강의 찜하기 토글
  async function toggleCourseLike(
    courseId: string | number,
    isCurrentlyLiked: boolean,
  ) {
    try {
      if (isCurrentlyLiked) {
        // [수정] RESTful API 사용 (routes.json 매핑 활용)
        const response = await api.get(
          `/members/me/course-likes?courseId=${courseId}`,
        );
        const likeRecord = response.data[0];
        if (likeRecord) {
          await api.delete(`/members/me/course-likes/${likeRecord.id}`);
        }
      } else {
        await api.post("/members/me/course-likes", {
          courseId: String(courseId),
        });
      }

      // 로컬 상태 즉시 업데이트 (낙관적 업데이트)
      const updateState = (list: LearnerCourse[]) => {
        const target = list.find(
          (c) => String(c.courseId) === String(courseId),
        );
        if (target) target.isLiked = !isCurrentlyLiked;
      };
      updateState(workspaceCourses.value);
      updateState(myEnrolledCourses.value);
      updateState(myLikedCourses.value);
    } catch (error) {
      console.error("찜 기능 처리 실패:", error);
    }
  }

  // [C] 강의 수강 신청
  async function enrollCourse(
    courseId: string | number,
    memberId: string | number,
  ) {
    try {
      const payload = {
        courseId: String(courseId),
        memberId: String(memberId),
        status: 1,
        applied_at: new Date().toISOString(),
      };
      // RESTful: POST /enrollments
      await api.post("/enrollments", payload);
      return true;
    } catch (error) {
      console.error("수강 신청 실패:", error);
      return false;
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
      const mapped = [response.data];
      courseInfo.value = mapped[0] || null;
      return mapped[0] || null;
    } catch (error) {
      console.error("강의 상세 정보 로드 실패:", error);
      courseInfo.value = null;
      return null;
    }
  };

  // [R] 강의 세션 목록 조회 (수강생용)
  // 강사는 별도의 세션 관리 API를 사용할 수 있음
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
    // State
    myEnrolledCourses,
    workspaceCourses,
    myLikedCourses,
    isLoading,
    myCoursesAttendanceRate,
    courseInfo,
    courseMaterials,

    // Actions
    fetchMyEnrolledCourses,
    fetchCoursesByWorkspace,
    toggleCourseLike,
    enrollCourse,
    fetchCourseSessions,
    fetchMyCoursesAttendanceRate,
    fetchLikeCourses,
    fetchCourseInfo,
    fetchCourseMaterials,
  };
});

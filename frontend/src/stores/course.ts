import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type { Course, Material } from "@/types";

// 카테고리별 테마를 여기서 관리 (필요 시 export 혹은 컴포넌트 이동)
// const CATEGORY_THEME_MAP: Record<... > = { ... };

export const useCourseStore = defineStore("course", () => {
  const myEnrolledCourses = ref<Course[]>([]);
  const workspaceCourses = ref<Course[]>([]);
  const myLikedCourses = ref<Course[]>([]);
  const courseInfo = ref<Course | null>(null);
  const isLoading = ref(false);
  const myCoursesAttendanceRate = ref<number | null>(null);

  // materialsCache: courseId -> Material[]
  const materialsCache = ref<Map<string | number, Material[]>>(new Map());
  const courseMaterials = ref<Material[]>([]);

  // Optimization: Track last fetched workspace ID to avoid refetching same data
  const lastWorkspaceId = ref<string | number | null>(null);

  // API 응답을 Course 타입으로 변환하는 함수
  const mapCourseData = (data: any[]): Course[] => {
    return data.map((course: any) => ({
      course_id: course.course_id || course.id,
      workspace_id: course.workspace_id,
      title: course.title,
      description: course.description || "",
      tag: course.tag,
      sub_category: course.sub_category || course.sub_tag,
      category: course.category,
      enrollment_status: course.status, // Assuming backend sends status correctly mapped or processed here
      course_status: course.status, // Simplification for mock
      progress_rate: course.progress_rate ?? course.progress ?? 0,
      current_lessons: course.current_lessons ?? course.currentLessons ?? 0,
      total_lessons: course.total_lessons ?? course.totalLessons ?? 0,
      is_mandatory: course.is_mandatory,
      d_day: course.d_day,
      is_liked: course.is_liked,
      duration: course.duration,
      request_date: course.request_date,
    }));
  };

  // 현재 워크스페이스에서 제공하는 모든 강의 목록 가져오기
  async function fetchCoursesByWorkspace(
    workspaceId: string | number,
    forceRefresh = false,
  ) {
    if (
      !forceRefresh &&
      lastWorkspaceId.value === workspaceId &&
      workspaceCourses.value.length > 0
    ) {
      return; // Use cached data
    }

    isLoading.value = true;
    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);
      const allCourses = mapCourseData(response.data);
      // Filter if needed, though backend should ideally handle it
      workspaceCourses.value = allCourses.filter(
        (c) => String(c.workspace_id) === String(workspaceId),
      );
      lastWorkspaceId.value = workspaceId;
    } catch (error) {
      console.error("워크스페이스 강의 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // 내가 참가한 강의 목록 조회
  async function fetchMyEnrolledCourses() {
    isLoading.value = true;
    try {
      const response = await api.get("/members/me/courses");
      myEnrolledCourses.value = mapCourseData(response.data);
    } catch (error) {
      console.error("참가 강의 목록 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // 찜한 강의 목록
  async function fetchLikeCourses() {
    isLoading.value = true;
    try {
      const response = await api.get("/members/me/course-likes");
      myLikedCourses.value = mapCourseData(response.data);
    } catch (error) {
      console.error("찜한 강의 목록 로드 실패:", error);
    } finally {
      isLoading.value = false;
    }
  }

  // 강의 찜하기 및 취소
  async function toggleCourseLike(
    courseId: string | number,
    isCurrentlyLiked: boolean,
  ) {
    try {
      if (isCurrentlyLiked) {
        // Mock: Fetch likes to find ID then delete
        const response = await api.get(`/course_likes?courseId=${courseId}`);
        const likeRecord = response.data[0];
        if (likeRecord) {
          await api.delete(`/members/me/course-likes/${likeRecord.id}`);
        }
      } else {
        await api.post("/members/me/course-likes", {
          courseId: String(courseId),
        });
      }

      // Update local state
      const updateState = (list: Course[]) => {
        const target = list.find(
          (c) => String(c.course_id) === String(courseId),
        );
        if (target) target.is_liked = !isCurrentlyLiked;
      };
      updateState(workspaceCourses.value);
      updateState(myEnrolledCourses.value);
      updateState(myLikedCourses.value); // Also update liked list locally if present

      // Invalidate if needed, or simple toggle is enough for UI
    } catch (error) {
      console.error("찜 기능 처리 실패:", error);
    }
  }

  // 강의 수강 신청
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
      await api.post(`/courses/${courseId}/enroll`, payload);
      return true;
    } catch (error) {
      console.error("수강 신청 실패:", error);
      return false;
    }
  }

  // 출석률
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
      console.error("강의 출석률 로드 실패:", error);
      return null;
    }
  };

  // 코스 정보
  const fetchCourseInfo = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}`);
      const mapped = mapCourseData([response.data]);
      courseInfo.value = mapped[0] || null;
      return mapped[0] || null;
    } catch (error) {
      console.error("강의 정보 로드 실패:", error);
      courseInfo.value = null;
      return null;
    }
  };

  // 세션 정보
  const fetchCourseSessions = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}/sessions`);
      return response.data;
    } catch (error) {
      console.error("세션 데이터 로드 실패:", error);
      return [];
    }
  };

  // 강의 자료 가져오기 (Caching implemented)
  async function fetchCourseMaterials(
    courseId: string | number,
    forceRefresh = false,
  ) {
    // Check Cache
    if (!forceRefresh && materialsCache.value.has(courseId)) {
      courseMaterials.value = materialsCache.value.get(courseId) || [];
      return;
    }

    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${courseId}/materials`);
      const materials = response.data;

      // Update Cache and State
      materialsCache.value.set(courseId, materials);
      courseMaterials.value = materials;
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

import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type { Course } from "@/types";

// 카테고리별 테마를 여기서 관리
const CATEGORY_THEME_MAP: Record<
  string,
  "indigo" | "blue" | "slate" | "amber"
> = {
  ai: "indigo",
  data: "blue",
  security: "slate",
  marketing: "amber",
  default: "indigo",
};

export const useCourseStore = defineStore("course", () => {
  const myEnrolledCourses = ref<Course[]>([]);
  const workspaceCourses = ref<Course[]>([]);
  const myLikedCourses = ref<Course[]>([]);
  const courseInfo = ref<Course | null>(null);
  const isLoading = ref(false);
  const myCoursesAttendanceRate = ref<number | null>(null);

  // API 응답을 Course 타입으로 변환하는 함수
  const mapCourseData = (data: any[]): Course[] => {
    return data.map((course: any) => ({
      course_id: course.course_id || course.id, // Handle both for mock/backend compatibility
      workspace_id: course.workspace_id,
      title: course.title,
      description: course.description || "",
      tag: course.tag, // Frontend specific or mapped
      sub_category: course.sub_category || course.sub_tag,
      category: course.category,

      // Status mapping: Backend 'status' vs Enrollment 'status'
      // If backend sends 'status' as 'PREPARING'..., map it to course_status
      // If backend sends enrollment info, map to enrollment_status
      course_status: ["PREPARING", "INVITE", "PROCEED", "FINISH"].includes(
        course.status,
      )
        ? course.status
        : undefined,
      enrollment_status: ["recruiting", "pending_approval", "active"].includes(
        course.status,
      )
        ? course.status
        : undefined,
      // Note: In our current mock, 'status' does double duty.

      progress_rate:
        course.progress_rate !== undefined
          ? course.progress_rate
          : course.progress || 0,
      current_lessons:
        course.current_lessons !== undefined
          ? course.current_lessons
          : course.currentLessons || 0,
      total_lessons:
        course.total_lessons !== undefined
          ? course.total_lessons
          : course.totalLessons || 0,

      is_mandatory: course.is_mandatory,
      d_day: course.d_day,
      is_liked: course.is_liked,
      duration: course.duration,
      request_date: course.request_date,
    }));
  };

  // 현재 워크스페이스에서 제공하는 모든 강의 목록 가져오기
  async function fetchCoursesByWorkspace(workspaceId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);
      // 프론트엔드에서 필터링 (routes.json의 쿼리 파라미터 매핑이 작동하지 않음)
      const allCourses = mapCourseData(response.data);
      workspaceCourses.value = allCourses.filter(
        (c: any) => String(c.workspace_id) === String(workspaceId),
      );
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
  // 강의 찜하기 및 취소 (별도 찜 테이블 관리)
  // - isCurrentlyLiked: 현재 좋아요 상태 (UI에서 넘겨줌)
  async function toggleCourseLike(
    courseId: string | number,
    isCurrentlyLiked: boolean,
  ) {
    try {
      if (isCurrentlyLiked) {
        // 찜 취소: courseId로 레코드를 찾아서 삭제
        // Mock API Limitation: We can't easily find the like record ID by courseId without fetching.
        // For now, assume simple toggle logic or specific endpoint if available.
        // In real backend, 'DELETE /members/me/course-likes' with body might be supported or query param.
        const response = await api.get(`/course_likes?courseId=${courseId}`);
        const likeRecord = response.data[0];
        if (likeRecord) {
          await api.delete(`/members/me/course-likes/${likeRecord.id}`);
        }
      } else {
        // 찜 등록: POST 호출 (찜 테이블에 레코드 추가)
        await api.post("/members/me/course-likes", {
          courseId: String(courseId),
        });
      }
      // [UI 동기화] 서버 DB는 바뀌었으니, 현재 메모리에 있는 리스트들의 하트 상태도 반전시켜줍니다.
      const updateState = (list: Course[]) => {
        const target = list.find(
          (c) => String(c.course_id) === String(courseId),
        );
        if (target) target.is_liked = !isCurrentlyLiked;
      };
      updateState(workspaceCourses.value);
      updateState(myEnrolledCourses.value);
    } catch (error) {
      console.error("찜 기능 처리 실패:", error);
    }
  }

  // 강의 수강 신청 (별도 수강신청 테이블 관리)
  async function enrollCourse(
    courseId: string | number,
    memberId: string | number,
  ) {
    try {
      const payload = {
        courseId: String(courseId),
        memberId: String(memberId),
        status: 1, // 1: 승인 대기, 2: 승인 완료
        applied_at: new Date().toISOString(),
      };

      await api.post(`/courses/${courseId}/enroll`, payload);

      // 신청 후 강의 목록 갱신 (상태가 pending_approval로 변경되도록)
      // 실제로는 서버에서 status를 업데이트해서 보내줘야 함
      return true;
    } catch (error) {
      console.error("수강 신청 실패:", error);
      return false;
    }
  }
  //이번달 나의 코스의 평균 출석률
  const fetchMyCoursesAttendanceRate = async (
    year?: number,
    month?: number,
  ) => {
    try {
      // 매개변수 없으면 현재 연/월 사용
      const targetYear = year || new Date().getFullYear();
      const targetMonth = month || new Date().getMonth() + 1;

      const response = await api.get(
        `/members/me/courses/attendance-rate?year=${targetYear}&month=${targetMonth}`,
      );
      // json-server에서 배열로 반환하므로 첫 번째 요소 사용
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
  // 코스 기본정보 가져오기
  const fetchCourseInfo = async (courseId: string | number) => {
    try {
      const response = await api.get(`/courses/${courseId}`);
      const mapped = mapCourseData([response.data]);
      // mapped[0]이 undefined일 경우 null을 할당하도록 명시
      courseInfo.value = mapped[0] || null;
      return mapped[0] || null;
    } catch (error) {
      console.error("강의 정보 로드 실패:", error);
      courseInfo.value = null; // 에러 시에도 상태 초기화
      return null;
    }
  };
  // 코스 세션정보 가져오기
  const fetchCourseSessions = async (courseId: string | number) => {
    try {
      // 1. Try fetching from course_details to get curriculum (Mock Data Structure)
      try {
        const response = await api.get(`/course_details/${courseId}`);
        if (response.data && response.data.curriculum) {
          return response.data.curriculum.map((c: any, index: number) => ({
            session_id: c.id,
            title: c.title,
            description: c.description || "", // db.json might not have description
            type: c.type || (index === 1 ? "live" : "vod"), // Mock type for demo
            is_completed: c.participationRate === 100,
            order: index + 1,
            start_at: c.startTime,
            end_at: c.endTime,
            duration: "1시간 30분", // Mock duration
            date: c.startTime ? c.startTime.split("T")[0] : undefined,
          }));
        }
      } catch (innerError) {
        // Ignore fallback
      }

      // 2. Fallback to /sessions endpoint if defined
      const response = await api.get(`/courses/${courseId}/sessions`);
      return response.data;
    } catch (error) {
      console.error("세션 데이터 로드 실패:", error);
      return [];
    }
  };

  return {
    myEnrolledCourses,
    workspaceCourses,
    myLikedCourses,
    isLoading,
    myCoursesAttendanceRate,
    courseInfo,
    fetchMyEnrolledCourses,
    fetchCoursesByWorkspace,
    toggleCourseLike,
    enrollCourse,
    fetchCourseSessions,
    fetchMyCoursesAttendanceRate,
    fetchLikeCourses,
    fetchCourseInfo,
  };
});

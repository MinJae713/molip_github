import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import type { Course } from "@/types";

export const useTrainerCourseStore = defineStore("trainerCourse", () => {
  const workspaceAllCourses = ref<Course[]>([]); // 현재 워크스페이스의 모든 코스 목록
  const managedCourses = ref<Course[]>([]); // 강사가 관리하는 전체 코스 목록
  const currentManagedCourse = ref<Course | null>(null); // 현재 선택/관리 중인 코스 상세
  const isLoading = ref(false);

  // 워크스페이스의 모든 코스 목록 가져오기
  async function fetchWorkspaceAllCourses(workspaceId: string | number) {
    isLoading.value = true;

    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);

      workspaceAllCourses.value = response.data.map((c: any) => ({
        ...c,
      }));
    } catch (error) {
      console.error("워크스페이스 모든 코스 목록 로드 실패:", error);

      workspaceAllCourses.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // 내 코스 가져오기
  async function fetchMyCourses(workspaceId: string | number, role: string) {
    isLoading.value = true;
    // 디버깅용: 호출 시 어떤 값이 들어오는지 확인
    console.log(`[API Request] workspaceId: ${workspaceId}, role: ${role}`);
    try {
      //진짜경로는 실제 서버 적용할때 목업 서버 바보라서 계속 꼬임
      //const response = await api.get(`/workspaces/:workspaceId/me/courses?role=TRAINER`,
      const response = await api.get(`/mock_my_courses_unified`);
      // json-server 필터링 결과가 정상적으로 배열로 오는지 확인
      console.log("[API Success] 응답 데이터:", response.data);

      // 데이터가 존재할 때만 할당
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
        (c) => String(c.course_id) === String(courseId),
      );
      if (found) {
        currentManagedCourse.value = found;
      } else {
        const response = await api.get(`/courses/${courseId}`);
        currentManagedCourse.value = response.data;
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
    payload: Partial<Course>,
    workspace_id: string | number,
  ) {
    isLoading.value = true;
    try {
      // post 일 때 워크스페이스 생성
      await api.post("/courses", payload);
      // 목록 갱신
      await fetchMyCourses(workspace_id, "TRAINER");
      return true;
    } catch (error) {
      console.error("코스 생성 실패:", error);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    workspaceAllCourses,
    managedCourses,
    currentManagedCourse,
    isLoading,
    fetchWorkspaceAllCourses,
    fetchMyCourses,
    setManagedCourse,
    createCourse,
  };
});

import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import type { AdminCourse } from "@/types";

export const useAdminCourseStore = defineStore("adminCourse", () => {
    const workspaceCourses = ref<AdminCourse[]>([]);
    const isLoading = ref(false);

    // 워크스페이스의 모든 코스 목록 가져오기 (관리자용)
    async function fetchWorkspaceCourses(workspaceId: string | number) {
        if (!workspaceId) return;

        isLoading.value = true;
        try {
            const response = await api.get(`/workspaces/${workspaceId}/courses`);
            workspaceCourses.value = response.data;
            console.log("[Admin] 워크스페이스 강의 로드 성공:", workspaceCourses.value);
        } catch (error) {
            console.error("[Admin] 워크스페이스 강의 로드 실패:", error);
            workspaceCourses.value = [];
        } finally {
            isLoading.value = false;
        }
    }

    // 코스 상세 정보는 관리자의 경우 별도 로직이 필요하다면 추가 구현
    // 현재는 AdminStatTable에서 목록 조회만 주로 사용

    return {
        workspaceCourses,
        isLoading,
        fetchWorkspaceCourses,
    };
});

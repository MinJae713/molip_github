import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import type { Session } from "@/types";

export const useTrainerSessionStore = defineStore("trainerSession", () => {
  // 1. 상태 (State)
  const managedSessions = ref<Session[]>([]); // 특정 코스의 전체 세션 목록 (관리용)
  const currentManagedSession = ref<Session | null>(null); // 현재 편집/관리 중인 세션
  const isLoading = ref(false);

  // 2. 액션 (Actions)

  // [R] 관리 중인 코스의 세션 목록 조회
  async function fetchManagedSessions(courseId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/courses/${courseId}/sessions`);
      managedSessions.value = response.data;
    } catch (error) {
      console.error("세션 목록 로드 실패:", error);
      managedSessions.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // [R] 단일 세션 상세 정보 (수정용)
  async function fetchSessionDetail(sessionId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/sessions/${sessionId}`);
      currentManagedSession.value = response.data;
    } catch (error) {
      console.error("세션 상세 로드 실패:", error);
      currentManagedSession.value = null;
    } finally {
      isLoading.value = false;
    }
  }

  // [C] 세션 생성
  async function createSession(
    courseId: string | number,
    payload: Partial<Session>,
  ) {
    try {
      await api.post(`/courses/${courseId}/sessions`, {
        ...payload,
        course_id: Number(courseId),
      });
      await fetchManagedSessions(courseId); // 목록 갱신
      return true;
    } catch (error) {
      console.error("세션 생성 실패:", error);
      return false;
    }
  }

  // [U] 세션 수정
  async function updateSession(
    sessionId: string | number,
    payload: Partial<Session>,
  ) {
    try {
      await api.patch(`/sessions/${sessionId}`, payload);
      return true;
    } catch (error) {
      console.error("세션 수정 실패:", error);
      return false;
    }
  }

  // [D] 세션 삭제
  async function deleteSession(sessionId: string | number) {
    try {
      await api.delete(`/sessions/${sessionId}`);
      return true;
    } catch (error) {
      console.error("세션 삭제 실패:", error);
      return false;
    }
  }

  // [U] 라이브 세션 시작/종료 상태 변경 (강사 전용)
  async function updateSessionState(
    sessionId: string | number,
    state: "live" | "ended" | "upcoming",
  ) {
    return updateSession(sessionId, { state });
  }

  return {
    managedSessions,
    currentManagedSession,
    isLoading,
    fetchManagedSessions,
    fetchSessionDetail,
    createSession,
    updateSession,
    deleteSession,
    updateSessionState,
  };
});

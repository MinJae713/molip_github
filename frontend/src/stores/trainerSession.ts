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

  // Helper
  const mapSession = (data: any): Session => ({
    sessionId: data.session_id || data.sessionId || data.id,
    courseId: data.course_id || data.courseId,
    title: data.title,
    description: data.description,
    state: data.state,
    startAt: data.start_at || data.startAt,
    endAt: data.end_at || data.endAt,
    date: data.date,
    duration: data.duration,
    isCompleted: data.is_completed ?? data.isCompleted,
    janusRoomId: data.janus_room_id || data.janusRoomId,
    pin: data.pin,
  });

  // 관리 중인 코스의 세션 목록 조회
  async function fetchManagedSessions(
    workspaceId: string | number,
    courseId: string | number,
  ) {
    isLoading.value = true;
    try {
      const response = await api.get(`/session?courseId=${courseId}`);
      managedSessions.value = Array.isArray(response.data)
        ? response.data.map(mapSession)
        : [];
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
      const response = await api.get(`/session/${sessionId}/simple`);
      currentManagedSession.value = mapSession(response.data);
    } catch (error) {
      console.error("세션 상세 로드 실패:", error);
      currentManagedSession.value = null;
    } finally {
      isLoading.value = false;
    }
  }

  // 세션 생성
  async function createSession(
    workspaceId: string | number, // used for refresh
    courseId: string | number,
    payload: any,
  ) {
    try {
      const requestData = {
        courseId: Number(courseId),
        title: payload.title,
        description: payload.description,
        date: payload.date,
        startAt: payload.startAt,
        endAt: payload.endAt,
        useFocus: payload.useFocus,
        useEmotion: payload.useEmotion,
        useQuiz: payload.useQuiz,
        quizCount: payload.quizCount || 0,
      };

      console.log("[createSession] Request Body:", requestData);
      const response = await api.post(`/session`, requestData);
      console.log("[createSession] Response:", response.data);
      // 3. 목록 갱신
      await fetchManagedSessions(workspaceId, courseId);

      // 성공 시 sessionId 반환 (혹은 true)
      return response.data;
    } catch (error) {
      console.error("세션 생성 실패:", error);
      return false;
    }
  }

  // 세션 수정
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

  // 세션 삭제
  async function deleteSession(sessionId: string | number) {
    try {
      await api.delete(`/session/${sessionId}`);
      return true;
    } catch (error) {
      console.error("세션 삭제 실패:", error);
      return false;
    }
  }

  // 라이브 세션 시작/종료 상태 변경 (강사 전용)
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

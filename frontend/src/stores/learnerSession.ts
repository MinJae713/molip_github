import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import type { Session } from "@/types";

export const useLearnerSessionStore = defineStore("learnerSession", () => {
  // --- 상태 (State) ---
  const sessionsList = ref<Session[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const currentSessionMaterials = ref<any[]>([]);
  const currentSessionInfo = ref<Session | null>(null);

  // [Helper] 데이터 가공 함수
  const transformSession = (session: any) => {
    const now = new Date();
    const { date, startAt, endAt, janusRoomId } = session;

    // 1. 상태(state) 판별: janusRoomId 유무 + 시간
    let state = 'upcoming'; 
    const startDateTime = new Date(`${date}T${startAt}`);
    const endDateTime = new Date(`${date}T${endAt}`);
    const bufferStartTime = new Date(startDateTime.getTime() - 10 * 60 * 1000); // 시작 10분 전

    // 1. 강제 종료 시간 (종료 예정 1시간 후)
    const forceCloseTime = new Date(endDateTime.getTime() + 60 * 60 * 1000);
    const hasRoom = !!janusRoomId;

    // --- 상태 판별 로직 수정 ---

    // [1순위] 이미 강제 종료 시간(1시간 여유분)조차 지났다면? 무조건 종료!
    if (now > forceCloseTime) {
      state = 'ended';
    } 
    // [2순위] 강사가 방을 닫았고(ID 없음), 종료 예정 시간을 넘겼다면? 종료!
    else if (now > endDateTime && !hasRoom) {
      state = 'ended';
    }
    // [3순위] 방이 열려있고, 시작 10분 전부터~강제 종료 시간 사이라면? 라이브!
    // (이 조건 덕분에 10분 늦게 끝나도 1시간 안에는 LIVE가 유지됩니다.)
    else if (hasRoom && now >= bufferStartTime) {
      state = 'live';
    }
    // [4순위] 그 외 모든 경우는 시작 전!
    else {
      state = 'upcoming';
    }

    // 2. 지속시간(duration) 계산
    let duration = '';
    if (startAt && endAt) {
      const [sH, sM] = startAt.split(':').map(Number);
      const [eH, eM] = endAt.split(':').map(Number);
      const diff = (eH * 60 + eM) - (sH * 60 + sM);
      const h = Math.floor(diff / 60);
      const m = diff % 60;
      if (h > 0) duration += `${h}시간 `;
      if (m > 0) duration += `${m}분`;
    }

    return { ...session, state, duration: duration.trim() };
  };

  // --- 액션 (Actions) ---

  // 세션 상세정보 가져오기
  async function fetchSessionInfo(sessionId: string | number) {
    isLoading.value = true;
    try {
      // 명세서에 맞춰 엔드포인트 유지 (/session/{id}/detail)
      const response = await api.get(`/session/${sessionId}/detail`);
      currentSessionInfo.value = transformSession(response.data);
    } catch (err: any) {
      console.error("세션 상세정보 로드 실패:", err);
      error.value = err;
    } finally {
      isLoading.value = false;
    }
  }

  // 세션별 자료 가져오기
  async function fetchSessionMaterials(sessionId: string | number) {
    isLoading.value = true;
    currentSessionMaterials.value = [];
    try {
      const response = await api.get(`/sessions/${sessionId}/materials`);
      currentSessionMaterials.value = response.data;
    } catch (err: any) {
      console.error("세션 자료 로드 실패:", err);
      error.value = err;
    } finally {
      isLoading.value = false;
    }
  }

  // 코스번호 기반 세션리스트 제공
  async function fetchSessionsList(courseId: string | number) {
    isLoading.value = true;
    error.value = null;
    sessionsList.value = []; 

    try {
      const response = await api.get("/session", {
        params: { courseId: courseId },
      });
      // 데이터 가공 후 저장
      sessionsList.value = response.data.map(transformSession);
    } catch (err: any) {
      console.error("세션 리스트 로드 실패:", err);
      error.value = err;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    sessionsList,
    isLoading,
    error,
    currentSessionInfo,
    currentSessionMaterials,
    fetchSessionsList,
    fetchSessionMaterials,
    fetchSessionInfo,
  };
});
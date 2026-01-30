import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type { Session } from "@/types";

export const useLearnerSessionStore = defineStore("learnerSession", () => {
  // 제공 데이터
  const sessionsList = ref<Session[]>([]);
  const currentSession = ref<Session | null>(null);
  // 로딩상태관리
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const currentSessionMaterials = ref<any[]>([]);
  const currentSessionInfo = ref<Session | null>(null);

  // 세션번호로 세션 정보 가져오기
  async function fetchSessionInfo(sessionId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/sessions/${sessionId}`);
      currentSessionInfo.value = response.data;
    } catch (err: any) {
      console.error("강의 세션 불러오기 실패!: ", err);
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
      console.error("강의 세션 자료 불러오기 실패!: ", err);
      error.value = err;
    } finally {
      isLoading.value = false;
    }
  }

  //코스번호 기반 세션리스트 제공
  async function fetchSessionsList(
    courseId: string | number,
    workspaceId: string | number,
  ) {
    isLoading.value = true;
    error.value = null;
    sessionsList.value = []; // 이전 데이터 초기화

    try {
      const response = await api.get("/session", {
        params: {
          courseId: courseId,
        },
      });
      sessionsList.value = response.data;
    } catch (err: any) {
      console.error("강의 세션 불러오기 실패!: ", err);
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

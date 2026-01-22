import { defineStore } from "pinia";
import { ref } from "vue";

import api from "@/api";
import type { Session } from "@/types";

export const useSessionStore = defineStore("session", () => {
  // 제공 데이터
  const sessionsList = ref<Session[]>([]);
  // 로딩상태관리
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  //세션번호 기반 세션리스트 제공
  async function fetchSessionsList(
    courseId: string | number,
    workspaceId: string | number,
  ) {
    isLoading.value = true;
    error.value = null;
    sessionsList.value = []; // 이전 데이터 초기화

    try {
      const response = await api.get(
        `/workspace/${workspaceId}/learner/classroom/${courseId}`,
      );
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
    fetchSessionsList,
  };
});

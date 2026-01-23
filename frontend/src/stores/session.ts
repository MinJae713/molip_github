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
  const currentSessionMaterials = ref<any[]>([]);

  // 세션별 자료 가져오기
  async function fetchSessionMaterials(sessionId: string | number) {
    isLoading.value = true;
    try {
      // Mock API call
      // const response = await api.get(`/sessions/${sessionId}/materials`);
      await new Promise((resolve) => setTimeout(resolve, 300));

      // Mock data based on sessionId to show different results
      if (String(sessionId) === "1" || String(sessionId) === "101") {
        currentSessionMaterials.value = [
          {
            fileId: 201,
            fileName: "Session1_Intro.pptx",
            fileSize: "5.2 MB",
            fileUrl: "#",
            fileType: "pptx",
          },
          {
            fileId: 202,
            fileName: "Session1_Lab.zip",
            fileSize: "15 MB",
            fileUrl: "#",
            fileType: "zip",
          },
        ];
      } else {
        currentSessionMaterials.value = [
          {
            fileId: 301,
            fileName: "Summary_Notes.txt",
            fileSize: "12 KB",
            fileUrl: "#",
            fileType: "txt",
          },
        ];
      }
    } catch (err: any) {
      console.error("세션 자료 로드 실패:", err);
      currentSessionMaterials.value = [];
    } finally {
      isLoading.value = false;
    }
  }

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
    fetchSessionMaterials,
    currentSessionMaterials,
  };
});

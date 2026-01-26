import { ref, watch } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { Workspace, WorkspaceStats } from "@/types";

export const useWorkspaceStore = defineStore("workspace", () => {
  //상태정보
  const myWorkspaces = ref<Workspace[]>([]);
  const currentWorkspace = ref<Workspace | null>(null);
  const isLoaded = ref(false);

  const workspaceStats = ref<WorkspaceStats | null>(null);

  // 내가 가입한 워크스테이션 목록 가져오기
  async function fetchMyWorkspaces(forceUpdate = false) {
    if (isLoaded.value && !forceUpdate) return;

    try {
      const response = await api.get("/members/me/workspaces");
      myWorkspaces.value = response.data;
      isLoaded.value = true;
    } catch (error) {
      console.error("내 워크스테이션 로드 실패:", error);
    }
  }
  // 이름/코드로 워크스테이션 검색
  async function searchWorkspaces(query: string): Promise<Workspace[]> {
    if (!query.trim()) return [];
    try {
      const response = await api.get("/workspaces/search", {
        params: { query },
      });
      return response.data;
    } catch (error) {
      console.error("워크스페이스 검색 실패:", error);
      return [];
    }
  }
  // 가입 신청
  async function joinWorkspace(
    workspaceId: string | number,
    role: string,
    userId: string | number,
  ) {
    try {
      const payload = {
        workspaceId,
        role,
        member_id: userId,
        status: 1, // 1: 대기 중
        applied_at: new Date().toISOString(),
      };
      await api.post(`/workspaces/${workspaceId}/join`, payload);
      // 가입 신청 후 내 목록을 다시 불러와서 UI를 갱신할 수 있습니다.
      await fetchMyWorkspaces(true);
      return true;
    } catch (error) {
      console.error("가입 신청 실패:", error);
      return false;
    }
  }

  // 특정 워크스페이스로 진입 시 정보 갱신 (ID 기반)
  // + 역할(Role) 설정 로직 추가
  async function setCurrentWorkspaceById(id: string | number) {
    // 0. 내 워크스페이스 목록이 없으면 먼저 로드
    if (!isLoaded.value) {
      await fetchMyWorkspaces();
    }

    // 1. 이미 갱신해둔 가입목록에서 현재 워크스테이션 찾기
    const found = myWorkspaces.value.find(
      (ws) => String(ws.workspace_id) === String(id),
    );

    if (found) {
      currentWorkspace.value = found;
    } else {
      // 없으면, 서버에서 기본 정보 가져오기 (권한이 없을 수도 있음)
      try {
        const response = await api.get(`/workspaces/${id}`);
        currentWorkspace.value = response.data;
      } catch (error) {
        currentWorkspace.value = null;
      }
    }
  }
  // 프론트단 유효성검사
  const isAccessible = (id: string | number) => {
    return myWorkspaces.value.some(
      (ws) => String(ws.workspace_id) === String(id) && ws.my_status === 2,
    );
  };
  // 학생용 - 워크스페이스 진입 시 상단 통계 요약정보 호출
  async function fetchWorkspaceStats(
    workspaceId: string | number,
    role: string,
  ) {
    try {
      // json-server의 관계형 쿼리 활용: /workspaces/1/my-stats?role="LEARNER or TRAINER"
      const response = await api.get(
        `/workspaces/${workspaceId}/my-stats?role=${role}`,
      );
      const data = Array.isArray(response.data)
        ? response.data[0]
        : response.data;
      workspaceStats.value = data || null;
    } catch (error) {
      console.error("통계 로드 실패:", error);
      workspaceStats.value = null;
    }
  }
  return {
    myWorkspaces,
    currentWorkspace,
    workspaceStats,
    fetchMyWorkspaces,
    searchWorkspaces,
    joinWorkspace,
    setCurrentWorkspaceById,
    isAccessible,
    fetchWorkspaceStats,
  };
});

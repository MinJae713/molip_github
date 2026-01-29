import { ref, watch, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { Workspace, WorkspaceStats } from "@/types";

export const useWorkspaceStore = defineStore("workspace", () => {
  // --- 상태 정보 (State) ---
  const myWorkspaces = ref<Workspace[]>([]);
  const currentWorkspace = ref<Workspace | null>(null);
  const isLoaded = ref(false);
  const workspaceStats = ref<WorkspaceStats | null>(null);

  // --- GETTERS (Computed) ---
  // 현재 워크스페이스에서의 내 역할
  const currentRole = computed(() => {
    return currentWorkspace.value?.my_role ?? null;
  });

  // --- ACTIONS ---

  // 내가 가입한 워크스테이션 목록 가져오기
  async function fetchMyWorkspaces(forceUpdate = false) {
    if (isLoaded.value && !forceUpdate) return;

    try {
      const response = await api.get("/workspaces/me");
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
      // json-server standard: q=query or name_like=query
      const response = await api.get("/workspaces", {
        params: { q: query },
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
      await api.post("/applications", payload);
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
    // 이미 갱신해둔 가입목록에서 현재 워크스테이션 찾기
    const found = myWorkspaces.value.find(
      (ws) => String(ws.workspace_id) === String(id),
    );

    if (found) {
      currentWorkspace.value = found;
    } else {
      // 없으면,서버에서 기본 정보 가져오기
      try {
        const response = await api.get(`/workspaces/${id}`);
        currentWorkspace.value = response.data;
      } catch (error) {
        currentWorkspace.value = null;
      }
    }
    // [중요] 정보를 찾았든 못 찾았든, 해당 ID에 대한 통계를 호출!
    // 진입 시점에 데이터 조각들을 모으는 방식입니다.
    if (id) {
      await fetchWorkspaceStats(id);
    }
  }

  // 프론트단 유효성검사 (fe-develop 기능)
  const isAccessible = (id: string | number) => {
    return myWorkspaces.value.some(
      (ws) => String(ws.workspace_id) === String(id) && ws.my_status === 2,
    );
  };
  // 워크스페이스 진입 시 상단 통계 요약정보 호출
  async function fetchWorkspaceStats(workspaceId: string | number) {
    try {
      // json-server의 관계형 쿼리 활용: /workspaces/1/my-stats
      const response = await api.get(`/workspaces/${workspaceId}/my-stats`);
      const data = Array.isArray(response.data)
        ? response.data[0]
        : response.data;
      workspaceStats.value = data || null;
    } catch (error) {
      console.error("통계 로드 실패:", error);
      workspaceStats.value = null;
    }
  }

  // 워크스페이스 정보 수정 (관리자용)
  async function updateWorkspace(
    id: string | number,
    payload: Partial<Workspace>,
  ) {
    try {
      const response = await api.patch(`/workspaces/${id}`, payload);
      if (
        currentWorkspace.value &&
        String(currentWorkspace.value.workspace_id) === String(id)
      ) {
        currentWorkspace.value = {
          ...currentWorkspace.value,
          ...response.data,
        };
      }
      await fetchMyWorkspaces(true);
      return true;
    } catch (error) {
      console.error("워크스페이스 수정 실패:", error);
      throw error;
    }
  }

  // 워크스페이스 삭제 (관리자용)
  async function deleteWorkspace(id: string | number) {
    try {
      await api.delete(`/workspaces/${id}`);
      myWorkspaces.value = myWorkspaces.value.filter(
        (ws) => String(ws.workspace_id) !== String(id),
      );
      if (
        currentWorkspace.value &&
        String(currentWorkspace.value.workspace_id) === String(id)
      ) {
        currentWorkspace.value = null;
      }
      return true;
    } catch (error) {
      console.error("워크스페이스 삭제 실패:", error);
      return false;
    }
  }

  return {
    myWorkspaces,
    currentWorkspace,
    currentRole, // computed 추가
    workspaceStats,
    isLoaded,
    fetchMyWorkspaces,
    searchWorkspaces,
    joinWorkspace,
    setCurrentWorkspaceById,
    isAccessible,
  };
});

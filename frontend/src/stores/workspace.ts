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
    return currentWorkspace.value?.myRole ?? null;
  });

  // --- Helper: Data Normalizer (Adapter) ---
  function mapApiToWorkspace(data: any): Workspace {
    // 1. Status 변환 (String -> Number)
    let status = 0;
    const rawStatus = data.my_status ?? data.myStatus;
    if (
      rawStatus === 2 ||
      rawStatus === "가입 완료" ||
      rawStatus === "ACTIVE"
    ) {
      status = 2; // 활동 중
    } else if (
      rawStatus === 1 ||
      rawStatus === "가입 대기" ||
      rawStatus === "PENDING"
    ) {
      status = 1; // 대기 중
    } else {
      status = 0; // 미가입
    }

    // 2. Role 변환 (Snake -> Camel)
    const role = data.my_role ?? data.myRole ?? null;

    // 3. ID 변환 & 객체 구성
    return {
      workspaceId: data.workspace_id || data.workspaceId,
      name: data.name,
      logoUrl: data.logo_url || data.logoUrl || null,
      description: data.description,
      memberCount: data.memberCount, // API 필드명 확인 필요 (보통 카멜)
      courseCount: data.courseCount,
      myStatus: status,
      myRole: role,
    };
  }

  // --- ACTIONS ---

  // 내가 가입한 워크스테이션 목록 가져오기
  async function fetchMyWorkspaces(forceUpdate = false) {
    if (isLoaded.value && !forceUpdate) return;

    try {
      const response = await api.get("/workspaces/me");
      console.log("asdFADFADFAFDS", response.data);
      // 배열 매핑 적용
      myWorkspaces.value = response.data.map(mapApiToWorkspace);
      isLoaded.value = true;
    } catch (error) {
      console.error("내 워크스테이션 로드 실패:", error);
    }
  }

  // 이름/코드로 워크스테이션 검색
  async function searchWorkspaces(query: string): Promise<Workspace[]> {
    if (!query.trim()) return [];
    try {
      const response = await api.get(`/workspaces/${query}/search`);
      console.log(`[Search] Query: ${query}, Response:`, response.data); // Debug log

      const data = response.data;
      if (Array.isArray(data)) {
        return data.map(mapApiToWorkspace);
      } else if (data && typeof data === "object") {
        // 단일 객체로 오는 경우 배열로 감싸서 처리
        return [mapApiToWorkspace(data)];
      }
      return [];
    } catch (error) {
      console.error("워크스페이스 검색 실패:", error);
      return [];
    }
  }

  // 가입 신청
  async function joinWorkspace(
    workspaceId: string | number,
    role: string,
    memberId: string | number,
    status: string,
    appliedAt: string,
  ) {
    try {
      console.log("가입 신청 페이로드:", {
        workspaceId,
        role,
        memberId,
        status,
        appliedAt,
      });
      const payload = {
        workspaceId,
        role,
        memberId,
        status,
        appliedAt,
      };
      await api.post("/workspaces/join", payload);
      await fetchMyWorkspaces(true);
      return true;
    } catch (error) {
      console.error("가입 신청 실패:", error);
      return false;
    }
  }

  // 특정 워크스페이스로 진입 시 정보 갱신 (ID 기반)
  async function setCurrentWorkspaceById(id: string | number) {
    const found = myWorkspaces.value.find(
      (ws) => String(ws.workspaceId) === String(id),
    );

    if (found) {
      currentWorkspace.value = found;
    } else {
      try {
        const response = await api.get(`/workspaces/${id}`);
        // 단일 객체 매핑 적용
        currentWorkspace.value = mapApiToWorkspace(response.data);
      } catch (error) {
        currentWorkspace.value = null;
      }
    }
    if (id) {
      await fetchWorkspaceStats(id);
    }
  }

  // 프론트단 유효성검사 (fe-develop 기능)
  const isAccessible = (id: string | number) => {
    return myWorkspaces.value.some(
      (ws) => String(ws.workspaceId) === String(id) && ws.myStatus === 2,
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
      console.log("통계 호출 성공!", workspaceStats.value);
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
      // 업데이트된 데이터도 매핑해서 상태 반영
      const updatedData = mapApiToWorkspace(response.data);

      if (
        currentWorkspace.value &&
        String(currentWorkspace.value.workspaceId) === String(id)
      ) {
        currentWorkspace.value = {
          ...currentWorkspace.value,
          ...updatedData,
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
        (ws) => String(ws.workspaceId) !== String(id),
      );
      if (
        currentWorkspace.value &&
        String(currentWorkspace.value.workspaceId) === String(id)
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
    currentRole,
    workspaceStats,
    isLoaded,
    fetchMyWorkspaces,
    searchWorkspaces,
    joinWorkspace,
    setCurrentWorkspaceById,
    isAccessible,
    fetchWorkspaceStats,
  };
});

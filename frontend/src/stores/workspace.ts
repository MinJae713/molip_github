import { ref, watch, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { Workspace, WorkspaceStats } from "@/types";

export const useWorkspaceStore = defineStore("workspace", () => {
  // --- 상태 정보 (State) ---
  const myWorkspaces = ref<Workspace[]>([]);
  const adminWorkspaces = ref<Workspace[]>([]); // 관리자용 전체 목록
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
      createdAt: data.createdAt || data.created_at, // API 필드 매핑
    };
  }

  // --- ACTIONS ---

  // [관리자용] 전체 워크스페이스 목록 조회
  async function fetchAllWorkspaces() {
    try {
      // 관리자는 'me'가 아닌 전체 목록을 조회하는 별도 API 사용 권장
      // 현재 백엔드 API 명세에 맞춰 '/workspaces' (전체) 또는 '/admin/workspaces' 등으로 조정 필요
      const response = await api.get("/workspaces");
      adminWorkspaces.value = response.data.map(mapApiToWorkspace);
    } catch (error) {
      console.error("전체 워크스페이스 로드 실패:", error);
      // Fallback: API가 없다면 기존 /workspaces/me라도 사용 (임시)
      // await fetchMyWorkspaces(true);
      // adminWorkspaces.value = [...myWorkspaces.value];
    }
  }

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
    // 관리자 목록에서도 검색 (createdAt 등 관리자 정보 포함 가능성)
    const found =
      adminWorkspaces.value.find((ws) => String(ws.workspaceId) === String(id)) ||
      myWorkspaces.value.find((ws) => String(ws.workspaceId) === String(id));

    if (found) {
      // 캐시된 데이터에 필수 정보(createdAt)가 없으면 상세 조회 시도
      if (!found.createdAt) {
        try {
          const response = await api.get(`/workspaces/${id}`);
          const detail = mapApiToWorkspace(response.data);
          // 기존 정보와 상세 정보 병합 (내 역할 정보 등 보존)
          currentWorkspace.value = { ...found, ...detail };
          console.log("워크스페이스 상세 정보 보강 완료:", currentWorkspace.value);
        } catch (error) {
          console.warn("워크스페이스 상세 보강 실패, 기존 데이터 사용");
          currentWorkspace.value = found;
        }
      } else {
        currentWorkspace.value = found;
      }
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

  // 워크스페이스 수정 (관리자용)
  async function updateWorkspace(
    id: string | number,
    payload: Partial<Workspace> & { logo_url?: string },
  ) {
    try {
      const apiPayload = {
        workspaceId: id,
        name: payload.name,
        description: payload.description,
        logoImage: payload.logoUrl || payload.logo_url,
      };

      const response = await api.patch("/workspaces", apiPayload);

      if (response.data.apiResult) {
        // 업데이트된 데이터 반영
        if (
          currentWorkspace.value &&
          String(currentWorkspace.value.workspaceId) === String(id)
        ) {
          currentWorkspace.value = {
            ...currentWorkspace.value,
            name: apiPayload.name || currentWorkspace.value.name,
            description:
              apiPayload.description || currentWorkspace.value.description,
            logoUrl: apiPayload.logoImage || currentWorkspace.value.logoUrl,
          };
        }
        await fetchMyWorkspaces(true);
        if (adminWorkspaces.value.length > 0) await fetchAllWorkspaces(); // 관리자 목록도 갱신
        return true;
      }
      return false;
    } catch (error) {
      console.error("워크스페이스 수정 실패:", error);
      throw error;
    }
  }

  // 워크스페이스 삭제 (관리자용)
  async function deleteWorkspace(id: string | number) {
    try {
      const response = await api.delete(`/workspaces/${id}`);

      if (response.data.apiResult) {
        myWorkspaces.value = myWorkspaces.value.filter(
          (ws) => String(ws.workspaceId) !== String(id),
        );
        // 관리자 목록에서도 제거
        adminWorkspaces.value = adminWorkspaces.value.filter(
          (ws) => String(ws.workspaceId) !== String(id)
        );

        if (
          currentWorkspace.value &&
          String(currentWorkspace.value.workspaceId) === String(id)
        ) {
          currentWorkspace.value = null;
        }
        return true;
      }
      return false;
    } catch (error) {
      console.error("워크스페이스 삭제 실패:", error);
      return false;
    }
  }

  // 워크스페이스 생성 (관리자용)
  async function createWorkspace(payload: { name: string; description: string }) {
    try {
      const response = await api.post("/workspaces", payload);
      const newWorkspace = mapApiToWorkspace(response.data);
      // 생성자는 자동으로 가입될 수 있으므로 myWorkspaces 갱신
      await fetchMyWorkspaces(true);
      // 관리자 목록 갱신
      if (adminWorkspaces.value.length > 0) adminWorkspaces.value.push(newWorkspace);

      return true; // 성공
    } catch (error) {
      console.error("워크스페이스 생성 실패:", error);
      throw error;
    }
  }

  return {
    myWorkspaces,
    adminWorkspaces,
    currentWorkspace,
    currentRole,
    workspaceStats,
    isLoaded,
    fetchMyWorkspaces,
    fetchAllWorkspaces,
    searchWorkspaces,
    joinWorkspace,
    setCurrentWorkspaceById,
    isAccessible,
    fetchWorkspaceStats,
    createWorkspace,
    updateWorkspace,
    deleteWorkspace
  };
});

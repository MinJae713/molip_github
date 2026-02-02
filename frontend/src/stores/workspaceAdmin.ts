import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import api from "@/api";

/* ==========================================================
 * TYPE DEFINITIONS
 * ========================================================== */

/** 1. 초대코드 (Invite Code) */
export interface JoinCode {
  joinCodeId: number;
  identifyValue: string; // 실제 초대 코드
  joinCodeName: string;  // 관리용 이름
  endedDate: string;     // 만료일 (ISO)
  createdAt?: string;    // 생성일 (ISO)
  status?: string;       // 클라이언트 계산용 (활성/만료 등)
}

export interface CreateJoinCodePayload {
  workspaceId: number;
  endedDate: string;
  joinCodeName: string;
}

export interface CreateJoinCodeResponse {
  identifyValue: string;
}

/** 2. 가입 신청 (Join Request) */
export interface JoinRequest {
  workspaceRegisterId: number; // PK
  workspaceId: number;
  memberId: number;
  email: string;
  name: string;
  profileImageUrl?: string;
  createdAt: string;       // 생성일 (신청일)
  updatedAt?: string;      // 수정일 (처리일)
  status: 'PENDING' | 'APPROVE' | 'REJECTED';
  approvedAt?: string;     // 승인일
  rejectReason?: string;   // 반려 사유
  rejectType?: 'AFFLIATION_MISMATCH' | 'INVITE_CODE_USE_MISS' | 'OVER_CAPACITY' | 'ETC'; // 반려 유형
}

/** 3. 워크스페이스 멤버 (Member) */
export interface WorkspaceMember {
  id: number; // memberId
  email: string;
  name: string;
  profileImageUrl?: string;
  role: 'TRAINER' | 'LEARNER';
  joinedAt?: string;
  status?: string;
}


export const useWorkspaceAdminStore = defineStore('workspaceAdmin', () => {

  /* ==========================================================
   * STATE
   * ========================================================== */

  const joinRequests = ref<JoinRequest[]>([]);
  const joinCodes = ref<JoinCode[]>([]);
  const members = ref<WorkspaceMember[]>([]);

  const isLoading = ref(false);
  const error = ref<string | null>(null);

  /* ==========================================================
   * GETTERS
   * ========================================================== */

  /** 승인 대기 중인 가입 신청 목록 */
  const pendingRequests = computed(() =>
    joinRequests.value.filter(req => req.status === 'PENDING')
  );

  /** 활성화된(만료되지 않은) 초대코드 목록 */
  const activeJoinCodes = computed(() => {
    const now = new Date();
    return joinCodes.value.filter(code => new Date(code.endedDate) >= now);
  });

  /* ==========================================================
   * ACTIONS
   * ========================================================== */

  /* ----------------------------------------------------------
   * 1. 초대코드 관리 (Invite Codes)
   * ---------------------------------------------------------- */

  /** 초대코드 목록 조회 */
  const fetchJoinCodes = async (workspaceId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const { data } = await api.get<JoinCode[]>(`/workspaces/${workspaceId}/joinCode`);
      joinCodes.value = data;
    } catch (e) {
      console.error("초대코드 목록 조회 실패", e);
      error.value = "초대코드 목록을 불러오지 못했습니다.";
      // throw e; // UI에서 예외 처리가 굳이 필요 없다면 생략 가능
    } finally {
      isLoading.value = false;
    }
  };

  /** 초대코드 생성 */
  const createJoinCode = async (payload: CreateJoinCodePayload): Promise<CreateJoinCodeResponse> => {
    isLoading.value = true;
    error.value = null;
    try {
      // 만료일 시간을 23:59:59.999로 설정
      const date = new Date(payload.endedDate);
      date.setHours(23, 59, 59, 999);

      const formattedPayload = {
        ...payload,
        endedDate: date.toISOString()
      };

      const { data } = await api.post<CreateJoinCodeResponse>("/workspaces/joinCode", formattedPayload);

      // 생성 후 목록 갱신
      await fetchJoinCodes(payload.workspaceId);
      return data;
    } catch (e) {
      console.error("초대코드 생성 실패", e);
      error.value = "초대코드 생성에 실패했습니다.";
      throw e; // 컴포넌트에서 알림 처리를 위해 throw
    } finally {
      isLoading.value = false;
    }
  };

  /** 초대코드 삭제 */
  const deleteJoinCode = async (joinCodeId: number, workspaceId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      await api.delete(`/workspaces/joinCode/${joinCodeId}`);
      await fetchJoinCodes(workspaceId);
    } catch (e) {
      console.error("초대코드 삭제 실패", e);
      error.value = "초대코드 삭제에 실패했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };


  /* ----------------------------------------------------------
   * 2. 가입 신청 관리 (Join Requests)
   * ---------------------------------------------------------- */

  /** 가입 신청 목록 조회 */
  const fetchJoinRequests = async (workspaceId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const { data } = await api.get<JoinRequest[]>(`/workspaces/${workspaceId}/register`);
      joinRequests.value = data;
    } catch (e) {
      console.error("가입 신청 목록 조회 실패", e);
      error.value = "가입 신청 목록을 불러오지 못했습니다.";
    } finally {
      isLoading.value = false;
    }
  };

  /** 가입 신청 상태 변경 (승인 / 반려) */
  const updateJoinRequestStatus = async (
    workspaceId: number,
    workspaceRegisterId: number,
    status: 'APPROVE' | 'REJECTED',
    rejectData?: { type: string; reason: string }
  ) => {
    isLoading.value = true; // 로딩 추가
    error.value = null;
    try {
      await api.patch(`/workspaces/register/status`, {
        workspaceRegisterId,
        workspaceId,
        status,
        rejectType: rejectData?.type || null,
        rejectReason: rejectData?.reason || ""
      });

      // 처리 후 목록 갱신
      await fetchJoinRequests(workspaceId);
    } catch (e) {
      console.error(`가입 신청 상태 변경 실패 (${status})`, e);
      error.value = `가입 신청 처리에 실패했습니다.`;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };


  /* ----------------------------------------------------------
   * 3. 멤버 관리 (Members)
   * ---------------------------------------------------------- */

  /** 워크스페이스 멤버 목록 조회 */
  const fetchMembers = async (workspaceId: number) => {
    isLoading.value = true;
    try {
      const { data } = await api.get<WorkspaceMember[]>(`/workspaces/${workspaceId}/members`);
      members.value = Array.isArray(data) ? data : [];
    } catch (e) {
      console.error("멤버 목록 조회 실패", e);
      members.value = [];
    } finally {
      isLoading.value = false;
    }
  };

  /** 멤버 역할 변경 */
  const changeMemberRole = async (memberId: number, role: string) => {
    isLoading.value = true;
    try {
      await api.patch(`/workspaces/changeRole`, {
        workspaceMemberId: memberId,
        workspaceMemberRole: role
      });

      // 성공 시 로컬 상태 업데이트 (불필요한 리패치 방지)
      const target = members.value.find(m => m.id === memberId);
      if (target) {
        target.role = role as any;
      }
    } catch (e) {
      console.error("권한 변경 실패", e);
      error.value = "권한 변경에 실패했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /** 멤버 비활성화 (추방/탈퇴 처리) */
  const inactiveMember = async (memberId: number, isSendMail: boolean = false) => {
    isLoading.value = true;
    try {
      await api.patch(`/workspaces/inactive`, {
        workspaceMemberId: memberId,
        isSendMail
      });

      // 성공 시 로컬 목록에서 제거
      members.value = members.value.filter(m => m.id !== memberId);
    } catch (e) {
      console.error("멤버 비활성화 실패", e);
      error.value = "멤버 비활성화에 실패했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 스토어 상태 초기화
   */
  const clear = () => {
    joinCodes.value = [];
    joinRequests.value = [];
    members.value = [];
    error.value = null;
    isLoading.value = false;
  };

  return {
    // State
    joinRequests,
    joinCodes,
    members,
    isLoading,
    error,

    // Getters
    pendingRequests,
    activeJoinCodes,

    // Actions
    fetchJoinCodes,
    createJoinCode,
    deleteJoinCode,

    fetchJoinRequests,
    updateJoinRequestStatus,

    fetchMembers,
    changeMemberRole,
    inactiveMember,

    clear
  };
});
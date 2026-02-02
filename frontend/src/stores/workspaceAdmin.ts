import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import api from "@/api";
import type { 
  JoinCode, 
  CreateJoinCodePayload, 
  CreateJoinCodeResponse, 
  JoinRequest, 
  WorkspaceMember 
} from '@/types';

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
    // 1. 기준 시간을 오늘 00:00:00으로 설정 (날짜 비교의 정확성)
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return joinCodes.value.filter(code => {
      if (!code.endedDate) return false;
      
      // 2. 만료일을 Date 객체로 변환
      const expirationDate = new Date(code.endedDate);
      
      // 3. 만료일이 오늘 00:00:00 이후라면 활성 상태로 간주
      return expirationDate >= today;
    });
  });
  
  /* ==========================================================
   * ACTIONS
   * ========================================================== */

  /* ----------------------------------------------------------
   * 1. 초대코드 관리 (Invite Codes)
   * ---------------------------------------------------------- */

  /** 초대코드 목록 조회 */
  const fetchJoinCodes = async (workspaceId: number) => {
    if (isLoading.value) return;
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
    if (isLoading.value) {
      return Promise.reject(new Error("이미 처리 중인 요청이 있습니다."));
    }
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
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;
    try {
      const { data } = await api.delete<{ apiResult: boolean }>(`/workspaces/joinCode/${joinCodeId}`);
      
      if (data.apiResult) {
        // 삭제 성공 시 목록 갱신
        await fetchJoinCodes(workspaceId);
      } else {
        throw new Error("삭제 처리에 실패했습니다.");
      }
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
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;
    try {
      const { data } = await api.get<JoinRequest[]>(`/workspaces/${workspaceId}/register`);
      // 데이터가 null로 올 경우를 대비한 방어 코드
      joinRequests.value = Array.isArray(data) ? data : [];
    } catch (e) {
      console.error("가입 신청 목록 조회 실패", e);
      error.value = "가입 신청 목록을 불러오지 못했습니다.";
      joinRequests.value = []; // 에러 시 빈 배열로 초기화
    } finally {
      isLoading.value = false;
    }
  };

  /** 가입 신청 상태 변경 (승인 / 반려) */
  const updateJoinRequestStatus = async (
    workspaceId: number,
    workspaceRegisterId: number,
    status: 'APPROVE' | 'REJECTED',
    rejectReason: string = "" // 사유만 받도록 단순화
  ) => {
    if (isLoading.value) return;
    isLoading.value = true; // 로딩 추가
    error.value = null;
    try {
      await api.patch(`/workspaces/register`, {
        workspaceRegisterId,
        status,
        rejectReason: status === 'REJECTED' ? rejectReason : ""
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
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;
    try {
      const { data } = await api.get<WorkspaceMember[]>(`/workspaces/${workspaceId}/member`);
      members.value = Array.isArray(data) ? data : [];
    } catch (e) {
      console.error("멤버 목록 조회 실패", e);
      error.value = "멤버 목록을 불러오지 못했습니다.";
      members.value = [];
    } finally {
      isLoading.value = false;
    }
  };

  /** 멤버 역할 변경 */
  const changeMemberRole = async (workspaceMemberId: number, newRole: 'TRAINER' | 'LEARNER') => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;

    try {
      // API 명세서 규격에 맞게 요청
      const { data } = await api.patch<{ apiResult: boolean }>('/workspaces/changeRole', {
        workspaceMemberId,
        workspaceMemberRole: newRole
      });

      if (data.apiResult) {
        // 💡 꿀팁: 성공 시 서버에서 다시 목록을 안 불러와도 되게 로컬 state 업데이트
        const member = members.value.find(m => m.workspaceMemberId === workspaceMemberId);
        if (member) {
          member.role = newRole;
        }
        console.log(`${workspaceMemberId}번 멤버의 역할이 ${newRole}로 변경되었습니다.`);
      } else {
        throw new Error("서버 처리 결과가 실패로 돌아왔습니다.");
      }
    } catch (e) {
      console.error("멤버 역할 변경 실패:", e);
      error.value = "역할을 변경하는 중 오류가 발생했습니다.";
      throw e; // 컴포넌트에서 알림(Toast 등)을 띄울 수 있도록 에러 토스
    } finally {
      isLoading.value = false;
    }
  };

  /** 멤버 비활성화 (추방/탈퇴 처리) */
  const inactiveMember = async (workspaceMemberId: number, isSendMail: boolean = false) => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;

    try {
      const { data } = await api.patch<{ apiResult: boolean }>('/workspaces/inactive', {
        workspaceMemberId,
        isSendMail
      });

      if (data.apiResult) {
        // 💡 삭제 대신 상태값만 업데이트
        const member = members.value.find(m => m.workspaceMemberId === workspaceMemberId);
        if (member) {
          member.status = 'INACTIVE'; // 상태를 INACTIVE로 변경
        }
        console.log(`${workspaceMemberId}번 멤버 상태가 비활성으로 변경되었습니다.`);
      } else {
        throw new Error("비활성화 처리에 실패했습니다.");
      }
    } catch (e) {
      console.error("멤버 비활성화 실패:", e);
      error.value = "멤버 상태 변경 중 오류가 발생했습니다.";
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
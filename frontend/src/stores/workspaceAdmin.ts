import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import api from "@/api";
import type {
  JoinCode,
  CreateJoinCodePayload,
  CreateJoinCodeResponse,
  JoinRequest,
  WorkspaceMember,
  MemberDetail
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
        // [UI 즉시 반영] 로컬 상태에서 제거
        joinCodes.value = joinCodes.value.filter(code => code.joinCodeId !== joinCodeId);

        // 삭제 성공 시 목록 갱신 (서버 동기화)
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
    status: 'APPROVE' | 'REJECTED'
  ) => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;
    try {
      // 해당 요청 정보 찾기 (payload 구성을 위해)
      const targetRequest = joinRequests.value.find(req => req.workspaceRegisterId === workspaceRegisterId);
      console.log('Target Request:', targetRequest); // 디버깅용 로그
      
      if (!targetRequest) {
        throw new Error("요청 정보를 찾을 수 없습니다.");
      }

      const payload = {
        workspaceId: workspaceId,
        memberId: targetRequest.memberId,
        role: targetRequest.role || 'LEARNER',
        status: status, // 'APPROVE' 또는 'REJECTED'
        appliedAt: new Date(Date.now() + 9 * 60 * 60 * 1000).toISOString()  // 현재 한국시간으로 전송 (승인/반려 시점) -> 백엔드 수정필요
      };

      // 3. POST 요청 전송
      await api.post(`/workspaces/join`, payload);

      // 4. 성공 시 로컬 상태 업데이트 (표준 방식: 서버 응답 확인 후 UI 갱신)
      // 목록에서 해당 요청을 찾아 상태를 변경
      const requestToUpdate = joinRequests.value.find(req => req.workspaceRegisterId === workspaceRegisterId);
      if (requestToUpdate) {
        requestToUpdate.status = status;
        // 승인/반려 시각 업데이트 (현재 시간으로 추정하거나 서버 응답값 사용 가능)
        requestToUpdate.approvedAt = new Date().toISOString();
      }

      // 선택적: 완전한 동기화를 위해 목록 다시 불러오기 (백그라운드 처리)
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

  /** 멤버 상세 정보 조회 */
  const fetchMemberDetail = async (workspaceId: number, workspaceMemberId: number): Promise<MemberDetail> => {
    // API Call
    try {
      const { data } = await api.get<MemberDetail>(`/workspaces/${workspaceId}/member/${workspaceMemberId}`);
      return data;
    } catch (e) {
      console.error("멤버 상세 조회 실패", e);
      throw e;
    }
  };

  /** 멤버 역할 변경 */
  const changeMemberRole = async (workspaceMemberId: number, newRole: 'TRAINER' | 'LEARNER') => {
    try {
      // API 명세서 규격에 맞게 요청
      const { data } = await api.patch<{ apiResult: boolean }>('/workspaces/member/role', {
        workspaceMemberId,
        role: newRole
      });

      if (data.apiResult) {
        // 성공 시 로컬 상태 업데이트
        const member = members.value.find(m => m.workspaceMemberId === workspaceMemberId);
        if (member) {
          member.role = newRole;
        }
        console.log(`${workspaceMemberId}번 멤버 역할이 ${newRole}로 변경되었습니다.`);
      } else {
        throw new Error("역할 변경 처리에 실패했습니다.");
      }
    } catch (e) {
      console.error("멤버 역할 변경 실패:", e);
      error.value = "멤버 역할 변경 중 오류가 발생했습니다.";
      throw e; // 컴포넌트에서 알림(Toast 등)을 띄울 수 있도록 에러 토스
    }
  };

  /** 멤버 역할 일괄 변경 (Bulk Action) */
  const changeMembersRole = async (workspaceMemberIds: number[], newRole: 'TRAINER' | 'LEARNER') => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;

    try {
      // 병렬 처리
      const promises = workspaceMemberIds.map(id => changeMemberRole(id, newRole));
      const results = await Promise.allSettled(promises);

      // 실패한 요청이 있는지 확인
      const failed = results.filter(r => r.status === 'rejected');
      if (failed.length > 0) {
        console.error(`${failed.length}건의 요청이 실패했습니다.`);
        throw new Error("일부 멤버의 역할 변경 처리에 실패했습니다.");
      }
    } catch (e) {
      console.error("일괄 역할 변경 실패:", e);
      error.value = "멤버 일괄 역할 변경 중 오류가 발생했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /** 멤버 상태 변경 (활성/비활성) - 단건 */
  const updateMemberStatus = async (workspaceMemberId: number, status: 'ACTIVE' | 'INACTIVE', isSendMail: boolean = false) => {
    try {
      const { data } = await api.patch<{ apiResult: boolean }>('/workspaces/activate', {
        workspaceMemberId,
        isSendMail,
        status
      });

      if (data.apiResult) {
        const member = members.value.find(m => m.workspaceMemberId === workspaceMemberId);
        if (member) {
          member.status = status;
        }
        console.log(`${workspaceMemberId}번 멤버 상태가 ${status}(으)로 변경되었습니다.`);
      } else {
        throw new Error("상태 변경 처리에 실패했습니다.");
      }
    } catch (e) {
      console.error("멤버 상태 변경 실패:", e);
      throw e;
    }
  };

  /** 멤버 상태 일괄 변경 (활성/비활성) */
  const updateMembersStatus = async (workspaceMemberIds: number[], status: 'ACTIVE' | 'INACTIVE', isSendMail: boolean = false) => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;

    try {
      // 병렬 처리
      const promises = workspaceMemberIds.map(id => updateMemberStatus(id, status, isSendMail));
      const results = await Promise.allSettled(promises);

      // 실패한 요청이 있는지 확인
      const failed = results.filter(r => r.status === 'rejected');
      if (failed.length > 0) {
        console.error(`${failed.length}건의 요청이 실패했습니다.`);
        throw new Error("일부 멤버의 상태 변경 처리에 실패했습니다.");
      }
    } catch (e) {
      console.error("일괄 상태 변경 실패:", e);
      error.value = "멤버 일괄 상태 변경 중 오류가 발생했습니다.";
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
    fetchMemberDetail,
    changeMemberRole,
    changeMembersRole,
    updateMemberStatus,
    updateMembersStatus,

    clear
  };
});
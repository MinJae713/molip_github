import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { WorkspaceMember } from '@/types';
import api from '@/api';

export const useMemberStore = defineStore('member', () => {
  const members = ref<WorkspaceMember[]>([]);
  const currentMemberDetail = ref<WorkspaceMember | null>(null);
  const joinRequests = ref<any[]>([]); 
  const isLoading = ref(false);

  // 1. 참여진 목록 조회
  async function fetchMembers(workspaceId: string | number) {
    isLoading.value = true;
    try {
      const { data } = await api.get<WorkspaceMember[]>(`/members`, {
        params: { workspace_id: workspaceId }
      });
      members.value = data;
    } catch (error) {
      console.error('참여진 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // 2. 특정 멤버 상세 정보 조회 (캐시 우선 로직)
  async function fetchMemberDetail(memberId: number) {
    const cached = members.value.find(m => m.id === memberId);
    if (cached) currentMemberDetail.value = cached;

    try {
      const { data } = await api.get<WorkspaceMember>(`/members/${memberId}`);
      currentMemberDetail.value = data;
    } catch (error) {
      console.error('상세 정보 로드 실패:', error);
    }
  }

  // 3. 단일 역할 변경
  async function updateMemberRole(memberId: number, newRole: string) {
    try {
      await api.patch(`/members/${memberId}`, { role: newRole });
      
      const targetMember = members.value.find(m => m.id === memberId);
      if (targetMember) {
        targetMember.role = newRole as any; 
      }
      
      if (currentMemberDetail.value?.id === memberId) {
        currentMemberDetail.value.role = newRole as any;
      }
      return true;
    } catch (error) {
      console.error('역할 변경 실패:', error);
      return false;
    }
  }

  // 4. 일괄 역할 변경
  async function updateMultipleRoles(ids: number[], newRole: string) {
    try {
      await Promise.all(ids.map(id => api.patch(`/members/${id}`, { role: newRole })));
      
      members.value = members.value.map(m => 
        ids.includes(m.id) ? { ...m, role: newRole as any } : m
      );

      if (currentMemberDetail.value && ids.includes(currentMemberDetail.value.id)) {
        currentMemberDetail.value.role = newRole as any;
      }
      return true;
    } catch (error) {
      console.error('일괄 역할 변경 실패:', error);
      return false;
    }
  }

  // 5. 단일 상태 변경 (ACTIVE <-> INACTIVE)
  async function updateMemberStatus(memberId: number, newStatus: 'ACTIVE' | 'INACTIVE') {
    try {
      await api.patch(`/members/${memberId}`, { status: newStatus });

      const targetMember = members.value.find(m => m.id === memberId);
      if (targetMember) {
        targetMember.status = newStatus as any;
      }

      if (currentMemberDetail.value?.id === memberId) {
        currentMemberDetail.value.status = newStatus as any;
      }
      return true;
    } catch (error) {
      console.error(`상태 변경(${newStatus}) 실패:`, error);
      return false;
    }
  }

  // 6. 일괄 상태 변경
  async function updateMultipleStatuses(ids: number[], newStatus: 'ACTIVE' | 'INACTIVE') {
    try {
      await Promise.all(ids.map(id => api.patch(`/members/${id}`, { status: newStatus })));

      members.value = members.value.map(m => 
        ids.includes(m.id) ? { ...m, status: newStatus as any } : m
      );

      if (currentMemberDetail.value && ids.includes(currentMemberDetail.value.id)) {
        currentMemberDetail.value.status = newStatus as any;
      }
      return true;
    } catch (error) {
      console.error('일괄 상태 변경 실패:', error);
      return false;
    }
  }
  
  // 7. [추가] 가입 신청 목록 조회 (Join 로직 포함)
  async function fetchJoinRequests(workspaceId: string | number) {
    isLoading.value = true;
    try {
      // 1) 신청 목록(workspace_registers) 가져오기
      const regRes = await api.get(`/workspace_registers`, {
        params: { workspace_id: workspaceId }
      });
      
      // 2) 신청 목록에 사용자 상세 정보(members) 합치기
      // (json-server 특성상 프론트에서 Join 처리를 해주는 것이 편합니다)
      const allMembersRes = await api.get<WorkspaceMember[]>(`/members`);
      
      joinRequests.value = regRes.data.map((reg: any) => {
        const userDetail = allMembersRes.data.find(m => String(m.id) === String(reg.member_id));
        return {
          ...reg,
          user: userDetail // 유저의 이름, 이메일, 이미지 등을 담음
        };
      });
    } catch (error) {
      console.error('가입 신청 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // 8. [추가] 가입 신청 승인/반려 처리
  async function updateRequestStatus(requestId: number, newStatus: number) {
    try {
      // status - 0: 대기, 1: 승인, 2: 반려
      await api.patch(`/workspace_registers/${requestId}`, { status: newStatus });
      
      // 로컬 상태 업데이트
      const target = joinRequests.value.find(r => r.id === requestId);
      if (target) target.status = newStatus;
      
      return true;
    } catch (error) {
      console.error('신청 상태 변경 실패:', error);
      return false;
    }
  }
  return {
    members,
    joinRequests,
    currentMemberDetail,
    isLoading,
    fetchMembers,
    fetchJoinRequests,
    updateRequestStatus,
    fetchMemberDetail,
    updateMemberRole,
    updateMultipleRoles,
    updateMemberStatus,
    updateMultipleStatuses
  };
});
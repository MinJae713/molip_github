import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { WorkspaceMember } from '@/types';
import api from '@/api';

export const useMemberStore = defineStore('member', () => {
  const members = ref<WorkspaceMember[]>([]);
  const currentMemberDetail = ref<WorkspaceMember | null>(null);
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

  return {
    members,
    currentMemberDetail,
    isLoading,
    fetchMembers,
    fetchMemberDetail,
    updateMemberRole,
    updateMultipleRoles,
    updateMemberStatus,
    updateMultipleStatuses
  };
});
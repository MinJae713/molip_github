<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import { useAdminCourseStore } from '@/stores/adminCourse'; // 강의 스토어 변경
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';
import MemberDetailModal from './MemberDetailModal.vue';
import RoleChangeModal from './RoleChangeModal.vue';
import ConfirmModal from '@/components/common/CommonComponents/ConfirmModal.vue';
import type { WorkspaceMember } from '@/types';

// 초기화
const route = useRoute();
const adminStore = useWorkspaceAdminStore();
const courseStore = useAdminCourseStore();
const workspaceId = route.params.id as string;

// 데이터 연결 (Store 상태 참조)
const members = computed(() => adminStore.members || []);
const isLoading = computed(() => adminStore.isLoading || courseStore.isLoading);

// 필터링 상태
const searchQuery = ref('');
const selectedCourseName = ref('전체 강의');

// 초기 데이터 로드 (멤버 및 워크스페이스 내 강의 목록)
// 멤버는 상위 뷰에서 로드하므로 여기서는 강의만 로드 가능, 혹은 둘 다 안전하게 로드
onMounted(async () => {
  if (workspaceId) {
    // memberStore.fetchMembers(workspaceId), -> Parent handles this, but refreshing logic might be needed
    await courseStore.fetchWorkspaceCourses(workspaceId);
  }
});

// 동적 강의 옵션 생성
const courseOptions = computed(() => {
  const titles = courseStore.workspaceCourses.map(c => c.title);
  return ['전체 강의', ...titles];
});

// 1. 검색 및 강의 필터링된 목록 (오류 방어 코드 포함)
const filteredMembers = computed(() => {
  return members.value.filter(m => {
    // 이름 검색: undefined 방지
    const name = m?.name || '';
    const matchName = name.toLowerCase().includes(searchQuery.value.toLowerCase());

    // 강의 필터: 멤버 객체의 courses 배열 내 선택한 강의명 포함 여부 확인
    const matchCourse = selectedCourseName.value === '전체 강의'
      ? true
      : (m.courses && m.courses.includes(selectedCourseName.value));

    return matchName && matchCourse;
  });
});

// 2. 체크박스 선택 로직
const selectedIds = ref<number[]>([]);
const isAllSelected = computed(() =>
  filteredMembers.value.length > 0 && selectedIds.value.length === filteredMembers.value.length
);

const selectedMembersData = computed(() =>
  members.value.filter(m => selectedIds.value.includes(m.workspaceMemberId))
);

const toggleAll = () => {
  selectedIds.value = isAllSelected.value ? [] : filteredMembers.value.map(m => m.workspaceMemberId);
};

// 3. 모달 상태 관리
const modals = ref({ detail: false, role: false, status: false });
const selectedMember = ref<WorkspaceMember | null>(null);

const openDetail = (member: WorkspaceMember) => {
  selectedMember.value = member;
  modals.value.detail = true;
};

const openRoleChange = (member?: WorkspaceMember) => {
  if (member) {
    selectedMember.value = member;
    selectedIds.value = [member.workspaceMemberId];
  }
  modals.value.role = true;
};

const openStatusConfirm = (member?: WorkspaceMember) => {
  if (member) {
    selectedMember.value = member;
    selectedIds.value = [member.workspaceMemberId];
    // 상세 모달에서 진입 시 상세 모달 닫기
    modals.value.detail = false;
  }
  modals.value.status = true;
};

// 4. 액션 실행 (상태 일괄 변경)
// 선택된 멤버들의 상태를 확인하여 액션 결정
// 모든 선택된 멤버가 INACTIVE일 경우 -> ACTIVE로 변경 (활성화)
// 그 외 (하나라도 ACTIVE가 있거나 섞여있음) -> INACTIVE로 변경 (비활성화)
const actionStatus = computed(() => {
  if (selectedMembersData.value.length === 0) return 'INACTIVE';
  const isAllInactive = selectedMembersData.value.every(m => m.status === 'INACTIVE');
  return isAllInactive ? 'ACTIVE' : 'INACTIVE';
});

const handleStatusChange = async () => {
  try {
    await adminStore.updateMembersStatus(selectedIds.value, actionStatus.value);
    
    // 성공 시 모달 닫기
    modals.value.status = false;
    selectedIds.value = [];
  } catch (e) {
    alert('일부 멤버 처리에 실패했습니다.');
  }
};

const handleRoleChange = async (payload: { ids: number[], role: string }) => {
  const { ids, role } = payload;
  const targetRole = role.toUpperCase() as 'TRAINER' | 'LEARNER';
  
  if (!ids || ids.length === 0) return;

  try {
    await adminStore.changeMembersRole(ids, targetRole);
    
    // 성공 시 모달 닫기 및 초기화
    modals.value.role = false;
    selectedIds.value = [];
    alert('역할이 변경되었습니다.');
  } catch (e) {
    console.error(e);
    alert('일부 멤버의 역할 변경에 실패했습니다.');
  }
};

// 5. 유틸리티
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
};
</script>

<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div class="p-4 border-b border-slate-100 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-3 flex-1 min-w-100">
        <div class="relative flex-1 max-w-xs">
          <span
            class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg font-light">search</span>
          <input v-model="searchQuery" type="text" placeholder="이름 검색..."
            class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/10 transition-all" />
        </div>
        <div class="relative min-w-40">
          <select v-model="selectedCourseName"
            class="w-full pl-3 pr-10 py-2 bg-white border border-slate-200 rounded-lg text-sm appearance-none focus:outline-none focus:ring-2 focus:ring-indigo-500/10 transition-all cursor-pointer">
            <option v-for="course in courseOptions" :key="course" :value="course">{{ course }}</option>
          </select>
          <span
            class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none text-lg">expand_more</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <span v-if="selectedIds.length > 0" class="text-xs text-slate-500 mr-2">
          <strong class="text-indigo-600">{{ selectedIds.length }}</strong>명 선택됨
        </span>
        <button @click="openRoleChange()" :disabled="selectedIds.length === 0"
          class="px-3 py-2 border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all">역할
          변경</button>
        
        <!-- 동적 상태 변경 버튼 -->
        <button @click="openStatusConfirm()" :disabled="selectedIds.length === 0"
          :class="[
            'px-3 py-2 border rounded-lg text-xs font-bold disabled:opacity-40 transition-all',
            actionStatus === 'ACTIVE' 
              ? 'border-emerald-50 text-emerald-600 hover:bg-emerald-50' 
              : 'border-red-50 text-red-600 hover:bg-red-50'
          ]">
          {{ actionStatus === 'ACTIVE' ? '계정 활성화' : '계정 비활성화' }}
        </button>
      </div>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50/50 border-b border-slate-100">
            <th class="p-4 w-12 text-center">
              <input type="checkbox" :checked="isAllSelected" @change="toggleAll"
                class="rounded border-slate-300 text-indigo-600 cursor-pointer" />
            </th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">참여진 정보</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider text-center">역할</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider text-center">가입일 / 최근 접속</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider text-center">상태</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-if="isLoading">
            <td colspan="5" class="p-20 text-center">
              <div class="flex flex-col items-center gap-2">
                <div class="size-8 border-4 border-indigo-500/20 border-t-indigo-600 rounded-full animate-spin"></div>
                <p class="text-xs text-slate-400 font-medium">데이터 로딩 중...</p>
              </div>
            </td>
          </tr>

          <template v-else>
            <tr v-for="member in filteredMembers" :key="member.workspaceMemberId" @click="openDetail(member)"
              class="hover:bg-indigo-50/30 transition-colors group cursor-pointer">
              <td class="p-4 text-center" @click.stop>
                <input type="checkbox" v-model="selectedIds" :value="member.workspaceMemberId"
                  class="rounded border-slate-300 text-indigo-600 cursor-pointer" />
              </td>
              <td class="p-4">
                <div class="flex items-center gap-3">
                  <div class="size-9 rounded-full bg-slate-100 overflow-hidden shrink-0 border border-slate-200">
                    <img v-if="member.profileImageUrl" :src="member.profileImageUrl"
                      class="size-full object-cover" />
                    <div v-else
                      class="size-full flex items-center justify-center bg-indigo-50 text-indigo-600 font-bold text-[11px]">
                      {{ (member.name || '?')[0] }}</div>
                  </div>
                  <div>
                    <div class="text-sm font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">{{
                      member.name || '이름 없음' }}</div>
                    <div class="text-[11px] text-slate-400 font-medium">{{ member.email }}</div>
                  </div>
                </div>
              </td>
              <td class="p-4 text-center">
                <StatusBadge v-if="member.role" :variant="(member.role.toLowerCase() as any)"
                  class="scale-90 origin-center" />
              </td>
              <td class="p-4 text-center">
                <div class="text-xs font-bold text-slate-600 mb-0.5">{{ formatDate(member.createdAt) }}</div>
                <div class="text-[10px] text-slate-400 font-medium">{{ member.connectedAt ? formatDate(member.connectedAt) : '접속 기록 없음' }}</div>
              </td>
              <td class="p-4 text-center">
                <StatusBadge v-if="member.status" :variant="(member.status.toLowerCase() as any)" show-dot />
              </td>
            </tr>
            <tr v-if="filteredMembers.length === 0">
              <td colspan="5" class="p-12 text-center text-slate-400 text-sm">검색 결과가 없습니다.</td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>


    <!-- Footer -->
    <div class="p-6 border-t border-slate-100 flex justify-between items-center bg-white">
      <p class="text-sm text-slate-500">
        <span v-if="searchQuery || selectedCourseName !== '전체 강의'">전체 {{ members.length }}명 중 </span>
        <span v-else>전체 </span>
        <span class="font-bold text-slate-900">{{ filteredMembers.length }}명</span>
        <span v-if="searchQuery || selectedCourseName !== '전체 강의'"> 표시됨</span>
      </p>
    </div>
  </div>

  <MemberDetailModal v-if="modals.detail" :isOpen="modals.detail" :member="selectedMember"
    @close="modals.detail = false" @open-role="openRoleChange" @open-status="openStatusConfirm" />

  <RoleChangeModal v-if="modals.role" :isOpen="modals.role" :selectedMembers="selectedMembersData"
    @close="modals.role = false" @confirm="handleRoleChange" />

  <ConfirmModal v-if="modals.status" :isOpen="modals.status" 
    :title="actionStatus === 'ACTIVE' ? '계정 활성화' : '계정 비활성화'" 
    :variant="actionStatus === 'ACTIVE' ? 'primary' : 'danger'"
    @close="modals.status = false" @confirm="handleStatusChange">
    <div class="flex flex-col items-center text-center py-2">
      <p class="text-sm text-slate-600 leading-relaxed">
        선택한 <strong class="text-slate-900">{{ selectedIds.length }}명</strong>의 계정을 
        <strong :class="actionStatus === 'ACTIVE' ? 'text-emerald-600' : 'text-red-600'">
          {{ actionStatus === 'ACTIVE' ? '활성화' : '비활성화' }}
        </strong>하시겠습니까?
      </p>
    </div>
  </ConfirmModal>
</template>
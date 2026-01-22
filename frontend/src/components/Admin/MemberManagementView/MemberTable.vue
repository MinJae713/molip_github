<script setup lang="ts">
import { ref, computed } from 'vue';
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';
import MemberDetailModal from './MemberDetailModal.vue';
import RoleChangeModal from './RoleChangeModal.vue';
import ConfirmModal from '@/components/common/CommonComponents/ConfirmModal.vue';
import type { WorkspaceMember } from '@/types';

// 임시 데이터 (실제는 store/props로 관리 권장)
const members = ref<WorkspaceMember[]>([
  { id: 1, name: '이정훈', email: 'jh.lee@techcorp.com', role: 'Admin', joinedAt: '2023.11.12', lastLogin: '2024.05.21 14:32', status: 'ACTIVE', profileImage: '', workspace_id: 1 },
  { id: 2, name: '김철수', email: 'cs.kim@techcorp.com', role: 'Instructor', joinedAt: '2024.01.05', lastLogin: '2024.05.20 09:15', status: 'ACTIVE', profileImage: '', workspace_id: 1 },
  { id: 3, name: '박영희', email: 'yh.park@techcorp.com', role: 'Learner', joinedAt: '2024.02.20', lastLogin: '2024.05.21 16:04', status: 'ACTIVE', profileImage: '', workspace_id: 1 },
]);

// 필터링 상태
const searchQuery = ref('');
const selectedCourse = ref('전체 강의');
const courseOptions = ['전체 강의', '리더십 역량 강화', '비즈니스 이메일', '영업 제안서 마스터'];

// 1. [검색 및 강의 필터링된 목록]
const filteredMembers = computed(() => {
  return members.value.filter(m => {
    const matchName = m.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    // 실제 구현 시에는 member 객체 내의 courses 데이터를 비교해야 함 (현재는 전체 표시)
    const matchCourse = selectedCourse.value === '전체 강의' ? true : true; 
    return matchName && matchCourse;
  });
});

// 2. 체크박스 선택 로직
const selectedIds = ref<number[]>([]);
const isAllSelected = computed(() => 
  filteredMembers.value.length > 0 && selectedIds.value.length === filteredMembers.value.length
);

const selectedMembersData = computed(() => 
  members.value.filter(m => selectedIds.value.includes(m.id))
);

const toggleAll = () => {
  selectedIds.value = isAllSelected.value ? [] : filteredMembers.value.map(m => m.id);
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
    selectedIds.value = [member.id];
  }
  modals.value.role = true;
};

const openStatusConfirm = (member?: WorkspaceMember) => {
  if (member) {
    selectedMember.value = member;
    selectedIds.value = [member.id];
  }
  modals.value.status = true;
};
</script>

<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div class="p-4 border-b border-slate-100 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-3 flex-1 min-w-100">
        <div class="relative flex-1 max-w-xs">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg font-light">search</span>
          <input 
            v-model="searchQuery"
            type="text" 
            placeholder="이름 검색..." 
            class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/10 transition-all" 
          />
        </div>
        <div class="relative min-w-40">
          <select 
            v-model="selectedCourse"
            class="w-full pl-3 pr-10 py-2 bg-white border border-slate-200 rounded-lg text-sm appearance-none focus:outline-none focus:ring-2 focus:ring-indigo-500/10 transition-all cursor-pointer"
          >
            <option v-for="course in courseOptions" :key="course" :value="course">{{ course }}</option>
          </select>
          <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none text-lg">expand_more</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <span v-if="selectedIds.length > 0" class="text-xs text-slate-500 mr-2">
          <strong class="text-indigo-600">{{ selectedIds.length }}</strong>명 선택됨
        </span>
        <button 
          @click="openRoleChange()"
          :disabled="selectedIds.length === 0" 
          class="px-3 py-2 border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all"
        >역할 변경</button>
        <button 
          @click="openStatusConfirm()"
          :disabled="selectedIds.length === 0" 
          class="px-3 py-2 border border-red-50 rounded-lg text-xs font-bold text-red-600 hover:bg-red-50 disabled:opacity-40 transition-all"
        >계정 비활성화</button>
      </div>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50/50 border-b border-slate-100">
            <th class="p-4 w-12 text-center">
              <input type="checkbox" :checked="isAllSelected" @change="toggleAll" class="rounded border-slate-300 text-indigo-600 cursor-pointer" />
            </th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">참여진 정보</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">역할</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">가입일 / 최근 접속</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider text-center">상태</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr 
            v-for="member in filteredMembers" 
            :key="member.id" 
            @click="openDetail(member)" 
            class="hover:bg-indigo-50/30 transition-colors group cursor-pointer"
          >
            <td class="p-4 text-center" @click.stop>
              <input type="checkbox" v-model="selectedIds" :value="member.id" class="rounded border-slate-300 text-indigo-600 cursor-pointer" />
            </td>
            <td class="p-4">
              <div class="flex items-center gap-3">
                <div class="size-9 rounded-full bg-slate-100 overflow-hidden shrink-0 border border-slate-200">
                  <img v-if="member.profileImage" :src="member.profileImage" class="size-full object-cover" />
                  <div v-else class="size-full flex items-center justify-center bg-indigo-50 text-indigo-600 font-bold text-[11px]">{{ member.name[0] }}</div>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">{{ member.name }}</div>
                  <div class="text-[11px] text-slate-400 font-medium">{{ member.email }}</div>
                </div>
              </div>
            </td>
            <td class="p-4">
              <StatusBadge :variant="(member.role.toLowerCase() as any)" class="scale-90 origin-left" />
            </td>
            <td class="p-4">
              <div class="text-xs font-bold text-slate-600 mb-0.5">{{ member.joinedAt }}</div>
              <div class="text-[10px] text-slate-400 font-medium">{{ member.lastLogin }}</div>
            </td>
            <td class="p-4 text-center">
              <StatusBadge :variant="(member.status.toLowerCase() as any)" show-dot />
            </td>
          </tr>
          <tr v-if="filteredMembers.length === 0">
            <td colspan="5" class="p-12 text-center text-slate-400 text-sm">검색 결과가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <MemberDetailModal 
    v-if="modals.detail"
    :isOpen="modals.detail" :member="selectedMember" 
    @close="modals.detail = false"
    @open-role="openRoleChange" @open-status="openStatusConfirm"
  />

  <RoleChangeModal
    v-if="modals.role"
    :isOpen="modals.role" :selectedMembers="selectedMembersData"
    @close="modals.role = false"
  />

  <ConfirmModal
    v-if="modals.status"
    :isOpen="modals.status" title="계정 비활성화" variant="danger"
    @close="modals.status = false"
  >
    <div class="flex flex-col items-center text-center py-2">
      <p class="text-sm text-slate-600 leading-relaxed">
        선택한 <strong class="text-slate-900">{{ selectedIds.length }}명</strong>의 계정을 비활성화하시겠습니까?<br>
        접속 권한은 즉시 박탈되나, 학습 데이터는 보관됩니다.
      </p>
    </div>
  </ConfirmModal>
</template>
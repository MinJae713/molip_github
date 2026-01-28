<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useMemberStore } from '@/stores/member';
import { useLearnerCourseStore } from '@/stores/learnerCourse'; // 강의 스토어 추가
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';
import MemberDetailModal from './MemberDetailModal.vue';
import RoleChangeModal from './RoleChangeModal.vue';
import ConfirmModal from '@/components/common/CommonComponents/ConfirmModal.vue';
import type { WorkspaceMember } from '@/types';

// 초기화
const route = useRoute();
const memberStore = useMemberStore();
const courseStore = useLearnerCourseStore();
const workspaceId = route.params.id as string;

// 데이터 연결 (Store 상태 참조)
const members = computed(() => memberStore.members || []);
const isLoading = computed(() => memberStore.isLoading || courseStore.isLoading);

// 필터링 상태
const searchQuery = ref('');
const selectedCourseName = ref('전체 강의');

// 초기 데이터 로드 (멤버 및 워크스페이스 내 강의 목록)
onMounted(async () => {
  if (workspaceId) {
    await Promise.all([
      memberStore.fetchMembers(workspaceId),
      courseStore.fetchCoursesByWorkspace(workspaceId)
    ]);
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
  members.value.filter(m => selectedIds.value.includes(m.member_id))
);

const toggleAll = () => {
  selectedIds.value = isAllSelected.value ? [] : filteredMembers.value.map(m => m.member_id);
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
    selectedIds.value = [member.member_id];
  }
  modals.value.role = true;
};

const openStatusConfirm = (member?: WorkspaceMember) => {
  if (member) {
    selectedMember.value = member;
    selectedIds.value = [member.member_id];
  }
  modals.value.status = true;
};

// 4. 액션 실행 (상태 일괄 변경)
const handleDeactivate = async () => {
  const success = await memberStore.updateMultipleStatuses(selectedIds.value, 'INACTIVE');
  if (success) {
    modals.value.status = false;
    selectedIds.value = [];
  }
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
        <button @click="openStatusConfirm()" :disabled="selectedIds.length === 0"
          class="px-3 py-2 border border-red-50 rounded-lg text-xs font-bold text-red-600 hover:bg-red-50 disabled:opacity-40 transition-all">계정
          비활성화</button>
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
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">역할</th>
            <th class="p-4 text-[11px] font-bold text-slate-500 uppercase tracking-wider">가입일 / 최근 접속</th>
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
            <tr v-for="member in filteredMembers" :key="member.member_id" @click="openDetail(member)"
              class="hover:bg-indigo-50/30 transition-colors group cursor-pointer">
              <td class="p-4 text-center" @click.stop>
                <input type="checkbox" v-model="selectedIds" :value="member.member_id"
                  class="rounded border-slate-300 text-indigo-600 cursor-pointer" />
              </td>
              <td class="p-4">
                <div class="flex items-center gap-3">
                  <div class="size-9 rounded-full bg-slate-100 overflow-hidden shrink-0 border border-slate-200">
                    <img v-if="member.profile_image_url" :src="member.profile_image_url"
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
              <td class="p-4">
                <StatusBadge v-if="member.role" :variant="(member.role.toLowerCase() as any)"
                  class="scale-90 origin-left" />
              </td>
              <td class="p-4">
                <div class="text-xs font-bold text-slate-600 mb-0.5">{{ member.joinedAt }}</div>
                <div class="text-[10px] text-slate-400 font-medium">{{ member.lastLogin }}</div>
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
  </div>

  <MemberDetailModal v-if="modals.detail" :isOpen="modals.detail" :member="selectedMember"
    @close="modals.detail = false" @open-role="openRoleChange" @open-status="openStatusConfirm" />

  <RoleChangeModal v-if="modals.role" :isOpen="modals.role" :selectedMembers="selectedMembersData"
    @close="modals.role = false" />

  <ConfirmModal v-if="modals.status" :isOpen="modals.status" title="계정 비활성화" variant="danger"
    @close="modals.status = false" @confirm="handleDeactivate">
    <div class="flex flex-col items-center text-center py-2">
      <p class="text-sm text-slate-600 leading-relaxed">
        선택한 <strong class="text-slate-900">{{ selectedIds.length }}명</strong>의 계정을 비활성화하시겠습니까?
      </p>
    </div>
  </ConfirmModal>
</template>
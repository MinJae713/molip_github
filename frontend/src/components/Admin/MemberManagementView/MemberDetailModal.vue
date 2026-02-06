<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import type { WorkspaceMember, MemberDetail } from '@/types';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import BaseModal from '@/components/common/CommonComponents/BaseModal.vue';
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';
import ProgressBar from '@/components/common/CommonComponents/ProgressBar.vue';

interface Props {
  isOpen: boolean;
  member: WorkspaceMember | null;
}

const props = defineProps<Props>();
const emit = defineEmits(['close', 'open-role', 'open-status']);
import { useRoute } from 'vue-router'; // Add import

// ...

const workspaceAdminStore = useWorkspaceAdminStore();
const route = useRoute(); // Use route

const isLoading = ref(false);
const memberDetail = ref<MemberDetail | null>(null);

const fetchDetail = async () => {
  if (!props.member) return;
  
  isLoading.value = true;
  try {
    const rawId = route.params.id;
    const workspaceId = Number(Array.isArray(rawId) ? rawId[0] : rawId);
    memberDetail.value = await workspaceAdminStore.fetchMemberDetail(workspaceId, props.member.workspaceMemberId);
  } catch (e) {
    console.error(e);
  } finally {
    isLoading.value = false;
  }
};

watch(() => props.isOpen, (newVal) => {
  if (newVal && props.member) {
    fetchDetail();
  } else {
    memberDetail.value = null;
  }
}, { immediate: true });

const roleVariant = computed(() => (props.member?.role?.toLowerCase() || memberDetail.value?.role?.toLowerCase() || 'learner') as any);
const statusVariant = computed(() => {
  const status = props.member?.status?.toLowerCase() || memberDetail.value?.status?.toLowerCase();
  return (status === 'active' ? 'active' : 'inactive') as any;
});

const enrolledCourses = computed(() => {
  if (!memberDetail.value?.courses) return [];
  return memberDetail.value.courses.map((c, idx) => {
    let statusVariant: any = 'ongoing';
    let statusLabel = '진행';

    if (c.status === 'FINISH') {
      statusVariant = 'completed';
      statusLabel = '종료';
    } else if (c.status === 'PREPARING') {
      statusVariant = 'upcoming'; 
      statusLabel = '예정';
    } else { // PROCEED or others default to ongoing
      statusVariant = 'ongoing';
      statusLabel = '진행';
    }

    return {
      id: idx,
      title: c.title,
      progress: c.totalCount > 0 ? Math.round((c.finishedCount / c.totalCount) * 100) : 0,
      status: statusVariant,
      statusLabel: statusLabel
    };
  });
});

const stats = computed(() => {
  const finished = memberDetail.value?.finishedCourse ?? 0;
  // 진행 중인 강의: 전체 강의 목록 중 status가 FINISH 또는 PREPARING이 아닌 것 (즉, PROCEED 등)
  // 혹은 리스트 로직과 동일하게 status가 FINISH가 아니고 PREPARING도 아닌 것을 ongoing으로 볼지 결정 필요
  // 리스트에서는 FINISH -> 종료, PREPARING -> 예정, 나머지 -> 진행
  
  const ongoing = memberDetail.value?.courses?.filter(c => c.status !== 'FINISH').length ?? 0;
  
  return {
    finished: `${finished}개`,
    ongoing: `${ongoing}개`,
  };
});
</script>

<template>
  <BaseModal :open="isOpen" @close="emit('close')" maxWidth="max-w-md">
    <template #header>
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-600 font-light text-xl">account_circle</span>
        <h3 class="text-lg font-bold text-slate-900">참여진 상세 정보</h3>
      </div>
    </template>

    <div v-if="isLoading" class="py-20 flex justify-center">
      <div class="size-8 border-4 border-indigo-500/20 border-t-indigo-600 rounded-full animate-spin"></div>
    </div>

    <template v-else>
      <div class="flex flex-col items-center mb-6">
        <div class="size-20 rounded-full p-1 ring-4 ring-indigo-500/5 mb-3 shadow-sm">
          <div class="size-full rounded-full bg-slate-50 overflow-hidden flex items-center justify-center border border-slate-100">
            <img v-if="memberDetail?.profileImageUrl" :src="memberDetail.profileImageUrl" class="size-full object-cover" />
            <span v-else class="material-symbols-outlined text-3xl text-slate-300 font-light">person</span>
          </div>
        </div>
        <h3 class="text-xl font-bold text-slate-900 mb-1">{{ memberDetail?.name || member?.name }}</h3>
        <div class="flex flex-col items-center gap-1.5">
          <StatusBadge :variant="roleVariant" class="px-3 py-0.5 text-xs" />
          <span class="text-xs text-slate-400 font-medium">{{ member?.email }}</span>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-0 border-y border-slate-50 mb-6 py-4">
        <div class="text-center border-r border-slate-50">
          <div class="text-[10px] font-bold text-slate-400 mb-0.5">참여 중인 강의</div>
          <div class="text-base font-bold text-slate-800">{{ stats.ongoing }}</div>
        </div>
        <div class="text-center border-r border-slate-50">
          <div class="text-[10px] font-bold text-slate-400 mb-0.5">완료한 강의</div>
          <div class="text-base font-bold text-indigo-600">{{ stats.finished }}</div>
        </div>
        <div class="flex flex-col items-center justify-center">
          <div class="text-[10px] font-bold text-slate-400 mb-1.5">상태</div>
          <StatusBadge :variant="statusVariant" show-dot class="scale-90" />
        </div>
      </div>

      <div class="w-full">
        <div class="flex items-center justify-between mb-3 px-1">
          <h4 class="text-xs font-bold text-slate-900 flex items-center gap-1.5">
            참여 중인 강의
            <span class="text-indigo-500 font-medium">{{ enrolledCourses.length }}</span>
          </h4>
        </div>

        <div class="space-y-2 max-h-60 overflow-y-auto pr-1 scrollbar-thin scrollbar-thumb-slate-200">
          <div v-for="course in enrolledCourses" :key="course.id" class="p-3 border border-slate-50 rounded-xl bg-white">
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-bold text-slate-700 truncate max-w-45">{{ course.title }}</span>
              <StatusBadge 
                :variant="course.status" 
                :label="course.statusLabel" 
                class="scale-90 origin-right"
                show-dot 
              />
            </div>
            <div class="flex flex-col gap-1">
              <ProgressBar :progress="course.progress" size="xs" />
            </div>
          </div>
          <div v-if="enrolledCourses.length === 0" class="text-center py-4 text-xs text-slate-400">
            참여 중인 강의가 없습니다.
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex items-center gap-2 w-full pt-2">
        <button 
          @click="emit('open-role', member)" 
          class="flex-1 px-3 py-2.5 border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-slate-50 transition-all flex items-center justify-center gap-1.5"
        >
          <span class="material-symbols-outlined text-base">manage_accounts</span>
          권한 변경
        </button>
      <button 
          @click="emit('open-status', member)" 
          :class="[
            'flex-1 px-3 py-2.5 border rounded-lg text-xs font-bold transition-all flex items-center justify-center gap-1.5',
            statusVariant === 'inactive' 
              ? 'border-emerald-50 text-emerald-600 hover:bg-emerald-50' 
              : 'border-red-50 text-red-500 hover:bg-red-50'
          ]"
        >
          <span class="material-symbols-outlined text-base">
            {{ statusVariant === 'inactive' ? 'person_check' : 'person_remove' }}
          </span>
          {{ statusVariant === 'inactive' ? '계정 활성화' : '계정 비활성화' }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<style scoped>
.scrollbar-thin::-webkit-scrollbar { width: 3px; }
.scrollbar-thumb-slate-200::-webkit-scrollbar-thumb { 
  background-color: #f1f5f9; 
  border-radius: 10px;
}
</style>
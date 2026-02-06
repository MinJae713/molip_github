<template>
  <aside class="w-64 bg-white border-r border-slate-200 flex-none hidden lg:flex flex-col overflow-y-auto z-30">
    <div class="p-4">
      <div class="flex items-center gap-3 px-3 py-3 bg-indigo-50/50 rounded-xl border border-indigo-100 mb-6">
        <div class="size-10 bg-white rounded-lg border border-indigo-100 flex items-center justify-center shadow-sm overflow-hidden">
          <img v-if="displayLogo" :src="displayLogo" :alt="displayName" class="w-full h-full object-contain" />
          <span v-else class="text-lg font-bold text-indigo-600">{{ displayName ? displayName.substring(0, 1).toUpperCase() : 'W' }}</span>
        </div>
        <div class="min-w-0">
          <div class="text-xs font-bold text-indigo-800 mb-0.5">Current Workspace</div>
          <div class="text-sm font-bold text-slate-900 truncate">{{ displayName }}</div>
        </div>
      </div>

      <nav class="space-y-1">
        <a v-for="item in displayedStudyMenus.slice(0, 1)" :key="item.id"
           :class="['sidebar-item', { active: currentActiveId === item.id }]" href="#"
           @click.prevent="handleNavigate(item.id)">
          <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
          {{ item.name }}
        </a>

        <template v-if="currentRole !== 'admin'">
          <div class="mt-4 mb-1 px-3">
            <span class="text-xs font-bold uppercase tracking-wider text-slate-400">학습 관리</span>
          </div>
          <a v-for="item in displayedStudyMenus.slice(1)" :key="item.id"
             :class="['sidebar-item', { active: currentActiveId === item.id }]" href="#"
             @click.prevent="handleNavigate(item.id)">
            <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
            {{ item.name }}
          </a>

          <div class="mt-4 mb-1 px-3">
            <span class="text-xs font-bold uppercase tracking-wider text-slate-400">커뮤니티</span>
          </div>
          <a v-for="item in communityMenus" :key="item.id"
             :class="['sidebar-item', { active: currentActiveId === item.id }]" href="#"
             @click.prevent="handleNavigate(item.id)">
            <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
            {{ item.name }}
          </a>
        </template>

        <div v-if="currentRole === 'admin'">
          <div class="mt-4 mb-1 px-3">
            <span class="text-xs font-bold uppercase tracking-wider text-slate-400">워크스페이스 관리</span>
          </div>
          <a v-for="item in adminMenus" :key="item.id"
             :class="['sidebar-item', { active: currentActiveId === item.id }]" href="#"
             @click.prevent="handleNavigate(item.id)">
            <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
            {{ item.name }}
          </a>
        </div>
      </nav>
    </div>

    <div class="mt-auto p-4 border-t border-slate-100">
      <a class="sidebar-item text-slate-500" href="#"><span class="material-symbols-outlined text-[20px]">settings</span>설정</a>
      <a class="sidebar-item text-slate-500" href="#"><span class="material-symbols-outlined text-[20px]">help</span>고객지원</a>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/stores/workspace';

interface MenuItem {
  id: string;
  name: string;
  icon: string;
  path: string;
}

const studyMenus: MenuItem[] = [
  { id: "dashboard", name: "대시보드", icon: "dashboard", path: "" },
  { id: "courses", name: "교육과정", icon: "library_books", path: "" },
  { id: "my-stats", name: "학습 통계", icon: "analytics", path: "" }
];

const communityMenus: MenuItem[] = [
  { id: "notice", name: "공지사항", icon: "campaign", path: "#" },
  { id: "qna", name: "Q&A", icon: "help_center", path: "#" }
];

// 라우터 index.ts에서 설정한 name과 정확히 일치시킴
const adminMenus: MenuItem[] = [
  { id: 'admin-settings', name: '워크스페이스 설정', icon: 'settings', path: 'workspaceAdminSettings' },
  { id: 'admin-members', name: '참여진 관리', icon: 'group', path: 'workspaceAdminMembers' },
  { id: 'admin-requests', name: '가입 신청 관리', icon: 'person_add', path: 'workspaceAdminRequest' },
  { id: 'admin-invite', name: '초대코드 센터', icon: 'vpn_key', path: 'workspaceAdminInvite' },
];

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();

const displayLogo = computed(() => workspaceStore.currentWorkspace?.logoUrl);
const displayName = computed(() => workspaceStore.currentWorkspace?.name || '워크스페이스 선택 안됨');

// 워크스페이스 정보 갱신 함수
const updateCurrentWorkspace = async () => {
  const workspaceId = route.params.id;
  if (workspaceId) {
    const id = Array.isArray(workspaceId) ? workspaceId[0] : workspaceId;
    if (id) {
        await workspaceStore.setCurrentWorkspaceById(id);
    }
  }
};

onMounted(() => {
  // 현재 워크스페이스 정보 동기화
  updateCurrentWorkspace();
});

// [추가] URL의 ID가 바뀔 때마다 (다른 워크스페이스 상세 이동 시) 정보를 갱신합니다.
watch(() => route.params.id, (newId) => {
  if (newId) updateCurrentWorkspace();
});

// 현재 사용자 역할 감지 (Store 기반)
const currentRole = computed(() => {
  const role = workspaceStore.currentRole;
  if (role) return role.toLowerCase(); // 'TRAINER' -> 'trainer'

  // 스토어 정보가 없을 때(새로고침 직후 등) 라우트 기반 폴백
  const name = route.name as string;
  if (name?.toLowerCase().includes('admin')) return 'admin';
  if (name?.toLowerCase().includes('trainer')) return 'trainer';
  return 'learner';
});

// 역할에 따라 메뉴 필터링
const displayedStudyMenus = computed(() => {
  // 모든 사용자에게 모든 학습 메뉴 표시
  return studyMenus;
});

// 현재 활성화된 메뉴 ID를 route 경로에 따라 자동으로 계산
const currentActiveId = computed(() => {
  const name = route.name as string;
  const path = route.path;

  // workspace 메인 경로면 'dashboard' 활성화
  if (name === 'workspaceAdmin' || name === 'workspaceLearner' || name === 'workspaceTrainer') return 'dashboard';
  // courses 경로면 'courses' 활성화
  if (path.includes('/courses') || path.includes('/classroom')) return 'courses';
  // my-stats 경로면 'my-stats' 활성화
  if (path.includes('/my-stats')) return 'my-stats';
  
  // 관리자 하위 메뉴 활성화 체크
  const activeAdminMenu = adminMenus.find(m => m.path === name);
  if (activeAdminMenu) return activeAdminMenu.id;

  return '';
});

const emit = defineEmits<{
  (e: 'navigate', id: string): void;
}>();

const handleNavigate = (id: string) => {
  const workspaceId = route.params.id as string;
  const role = currentRole.value; // Store 기반 역할

  // 1. 관리자 메뉴 처리
  const adminItem = adminMenus.find(m => m.id === id);
  if (adminItem && workspaceId) {
    router.push({ name: adminItem.path, params: { id: workspaceId } });
    return;
  }

  // 2. 공통 메뉴 처리
  if (id === 'dashboard' && workspaceId) {
    const target = role === 'admin' ? 'workspaceAdmin' : role === 'trainer' ? 'workspaceTrainer' : 'workspaceLearner';
    router.push({ name: target, params: { id: workspaceId } });
  } else if (id === 'courses' && workspaceId) {
    const target = role === 'trainer' ? 'workspaceTrainerCourses' : 'workspaceLearnerCourses';
    router.push({ name: target, params: { id: workspaceId } });
  } else if (id === 'my-stats' && workspaceId) {
    if (role === 'learner') {
        router.push({ name: 'workspaceLearnerMyStats', params: { id: workspaceId } });
    } else if (role === 'trainer') {
        router.push({ name: 'workspaceTrainerStatistics', params: { id: workspaceId } });
    }
  } else {
    // 다른 메뉴는 기존대로 emit 이벤트 발생
    emit('navigate', id);
  }
};
</script>

<style scoped>
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
  /* slate-600 */
  transition: all 0.2s;
  cursor: pointer;
}

.sidebar-item:hover {
  background-color: #f8fafc;
  /* slate-50 */
  color: #4f46e5;
  /* indigo-brand */
}

.sidebar-item.active {
  background-color: #eef2ff;
  /* indigo-50 */
  color: #4f46e5;
  /* indigo-brand */
  font-weight: 700;
}
</style>
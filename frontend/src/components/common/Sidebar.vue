<template>
    <aside class="w-64 bg-white border-r border-slate-200 flex-none hidden lg:flex flex-col overflow-y-auto z-30">
        <div class="p-4">
            <div class="flex items-center gap-3 px-3 py-3 bg-indigo-50/50 rounded-xl border border-indigo-100 mb-6">
                <div
                    class="size-10 bg-white rounded-lg border border-indigo-100 flex items-center justify-center shadow-sm overflow-hidden">
                    <img v-if="workspaceLogo" :src="workspaceLogo" :alt="workspaceName"
                        class="w-full h-full object-contain" />
                    <span v-else class="material-symbols-outlined text-indigo-600">domain</span>
                </div>
                <div class="min-w-0">
                    <div class="text-xs font-bold text-indigo-800 mb-0.5">Current Workspace</div>
                    <div class="text-sm font-bold text-slate-900 truncate">{{ workspaceName }}</div>
                </div>
            </div>
            <nav class="space-y-1">
                <a v-for="item in studyMenus.slice(0, 1)" :key="item.id"
                    :class="['sidebar-item', { active: currentActiveId === item.id }]" href="#"
                    @click.prevent="handleNavigate(item.id)">
                    <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
                    {{ item.name }}
                </a>

                <div class="mt-4 mb-1 px-3">
                    <span class="text-xs font-bold uppercase tracking-wider text-slate-400">학습 관리</span>
                </div>
                <a v-for="item in studyMenus.slice(1)" :key="item.id"
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
            </nav>
        </div>

        <div class="mt-auto p-4 border-t border-slate-100">
            <a class="sidebar-item text-slate-500" href="#">
                <span class="material-symbols-outlined text-[20px]">settings</span>
                설정
            </a>
            <a class="sidebar-item text-slate-500" href="#">
                <span class="material-symbols-outlined text-[20px]">help</span>
                고객지원
            </a>
        </div>
    </aside>
</template>

<script setup lang="ts">
import { onMounted, computed, watch, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/stores/workspace';

interface MenuItem {
    id: string;
    name: string;
    icon: string;
    path: string;
}

const studyMenus: MenuItem[] = [
    { id: "dashboard", name: "대시보드", icon: "dashboard", path: "#" },
    { id: "courses", name: "교육과정", icon: "library_books", path: "#" },
    { id: "stats", name: "학습 통계", icon: "analytics", path: "#" }
];

const communityMenus: MenuItem[] = [
    { id: "notice", name: "공지사항", icon: "campaign", path: "#" },
    { id: "qna", name: "Q&A", icon: "help_center", path: "#" }
];

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();

/**
 * [사고의 흐름]
 * 1. props로 전달받던 방식에서 Store 직접 참조 방식으로 변경합니다.
 * 2. computed를 사용하여 Store 데이터가 변하면 UI가 즉시 갱신되도록 합니다.
 */
const displayLogo = computed(() => workspaceStore.currentWorkspace?.logo_url);
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
    // 사이드바 메뉴 데이터 로드 (Removed fetch)
    // 현재 워크스페이스 정보 동기화
    updateCurrentWorkspace();
});


// [추가] URL의 ID가 바뀔 때마다 (다른 워크스페이스 상세 이동 시) 정보를 갱신합니다.
watch(() => route.params.id, (newId) => {
    if (newId) updateCurrentWorkspace();
});

interface Props {
    workspaceName?: string;
    workspaceLogo?: string;
    currentActiveId?: string;
}
// 값 없을 경우 기본값 설정
const props = withDefaults(defineProps<Props>(), {
    workspaceName: 'Naver',
    workspaceLogo: 'https://placehold.co/100x100/4f46e5/ffffff?text=TechCorp',
    currentActiveId: 'courses', // 기본 활성화 아이템
});

// 현재 활성화된 메뉴 ID를 route 경로에 따라 자동으로 계산
const currentActiveId = computed(() => {
    const path = route.path;

    // '/workspaceDetail/:id/myCourses' 경로면 'courses' 활성화
    if (path.includes('/myCourses')) {
        return 'courses';
    }
    // '/workspaceDetail/:id' 경로면 'dashboard' 활성화
    if (path.match(/\/workspaceDetail\/\d+$/)) {
        return 'dashboard';
    }


    // prop으로 전달된 값 또는 기본값 사용
    return props.currentActiveId;
});

const emit = defineEmits<{
    (e: 'navigate', id: string): void;
}>();

const handleNavigate = (id: string) => {
    const workspaceId = route.params.id;

    if (id === 'courses' && workspaceId) {
        // '교육과정' 메뉴 클릭 시 myCourses 경로로 이동
        router.push({ name: 'myCourses', params: { id: workspaceId } });
    } else if (id === 'dashboard' && workspaceId) {
        router.push({ name: 'workspaceDetail', params: { id: workspaceId } })
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
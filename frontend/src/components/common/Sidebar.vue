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
/**
 * 메뉴 아이템의 타입을 정의합니다.
 */
interface MenuItem {
    id: string;
    name: string;
    icon: string;
    path: string;
}

interface Props {
    workspaceName?: string;
    workspaceLogo?: string;
    currentActiveId?: string;
}
// 값 없을 경우 기본값 설정
const props = withDefaults(defineProps<Props>(), {
    workspaceName: 'Naver',
    workspaceLogo: 'https://i.namu.wiki/i/D1NyR0mdLBArSqS6T4OsULacKtn3bOPUGYQNEcLOUeH8hBqSW0FcFZg6gAOEaxf6qGVlTUf0EASXEbGpac4GolNcetSsiDzFTQG-Jq8rUVUM7KOWhSRyDoXrcC6PDOQ8EyLtd9junL9Pb9kF0eMJaw.svg',
    currentActiveId: 'courses', // 기본 활성화 아이템
});

// 메뉴 데이터 정의 (보통 서버나 외부 파일에서 관리)
const studyMenus: MenuItem[] = [
    { id: 'dashboard', name: '대시보드', icon: 'dashboard', path: '#' },
    { id: 'courses', name: '교육과정', icon: 'library_books', path: '#' },
    { id: 'stats', name: '학습 통계', icon: 'analytics', path: '#' },
];

const communityMenus: MenuItem[] = [
    { id: 'notice', name: '공지사항', icon: 'campaign', path: '#' },
    { id: 'qna', name: 'Q&A', icon: 'help_center', path: '#' },
];

const emit = defineEmits<{
    (e: 'navigate', id: string): void;
}>();

const handleNavigate = (id: string) => {
    emit('navigate', id);
};
</script>


<style scoped>
/* Tailwind @apply를 사용하려면 main.css나 프로젝트 공통 스타일에 정의되어 있어야 합니다.
  컴포넌트 독립성을 위해 기본 스타일을 scoped에 추가해둡니다.
*/
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
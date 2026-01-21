<template>
    <aside class="w-64 bg-white border-r border-slate-200 flex-none hidden lg:flex flex-col overflow-y-auto z-30">
        <div class="p-4">
            <nav class="space-y-1">
                <!-- 1. 내 정보 -->
                <a href="#" class="sidebar-item active">
                    <span class="material-symbols-outlined text-[20px]">manage_accounts</span>
                    내 정보 관리
                </a>
                <!-- 2. 수료증 보관함 -->
                <a href="#" class="sidebar-item">
                    <span class="material-symbols-outlined text-[20px]">verified</span>
                    수료증 보관함
                </a>
                <!-- 3. 수료증 진위확인 -->
                <a href="#" class="sidebar-item">
                    <span class="material-symbols-outlined text-[20px]">fact_check</span>
                    수료증 진위확인
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
    workspaceLogo: 'https://placehold.co/100x100/4f46e5/ffffff?text=TechCorp',
    currentActiveId: 'courses', // 기본 활성화 아이템
});

const emit = defineEmits<{
    (e: 'navigate', id: string): void;
}>();

const handleNavigate = (id: string) => {
    emit('navigate', id);
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
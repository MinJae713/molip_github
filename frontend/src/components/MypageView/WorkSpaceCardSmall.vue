<template>
    <div
        class="cursor-pointer flex items-center gap-3 px-3 py-3 bg-indigo-50/50 rounded-xl border border-indigo-100 mb-6">
        <div
            class="size-10 bg-white rounded-lg border border-indigo-100 flex items-center justify-center shadow-sm overflow-hidden">
            <img v-if="workspace.logo_url" :src="workspace.logo_url" :alt="workspace.name"
                class="w-full h-full object-contain" />
            <span v-else class="material-symbols-outlined text-indigo-600">domain</span>
        </div>
        <div class="min-w-0 flex flex-col items-start gap-1">
            <div class="text-sm font-bold text-slate-900 truncate w-full">
                {{ workspace.name }}
            </div>

            <span v-if="workspace.role === 'TRAINER'"
                class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-700 whitespace-nowrap leading-none">
                강사
            </span>
            <span v-else
                class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-700 whitespace-nowrap leading-none">
                수강생
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
interface workspace {
    id: number | string;
    name: string;
    logo_url?: string;
    role?: string;
    status?: number;
}
interface Props {
    workspace: workspace; // 객체 통째로 받기
}
const props = defineProps<Props>();

const emit = defineEmits<{
    (e: 'navigate', id: number | string): void;
}>();

const handleNavigate = (id: number | string) => {
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

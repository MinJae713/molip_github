<template>
    <article class="glass-panel p-6 flex flex-col hover:border-indigo-300 transition-all duration-300 group">
        <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-4">
                <div
                    :class="['size-14 rounded-lg border flex items-center justify-center overflow-hidden transition-colors', theme.bg, theme.border]">
                    <img v-if="workspace.logoUrl" :src="workspace.logoUrl" :alt="workspace.name"
                        class="w-full h-full object-cover" />
                </div>
                <div>
                    <h3 class="font-bold text-lg text-slate-900 group-hover:text-indigo-600 transition-colors">
                        {{ workspace.name }}
                    </h3>
                </div>
            </div>
            <span
                :class="['px-3 py-1 whitespace-nowrap text-xs font-bold rounded-full flex items-center gap-1 transition-colors', buttonConfig.badgeClass]">
                <span
                    :class="['size-2 rounded-full', buttonConfig.dotClass, { 'animate-pulse': workspace.myStatus === 2 }]"></span>
                {{ buttonConfig.text === '입장하기' ? '활동 중' : buttonConfig.text }}
            </span>
        </div>

        <p class="text-slate-600 text-sm mb-6 line-clamp-2 min-h-[2.5rem]">
            {{ workspace.description || 'No description available.' }}
        </p>
        <div class="mt-auto pt-4 border-t border-slate-100 flex items-center justify-between">
            <div class="flex items-center gap-2 text-sm text-slate-500 font-medium">
                <span class="material-symbols-outlined text-lg">group</span>
                <span>멤버 {{ (workspace.memberCount || 0).toLocaleString() }}명</span>
            </div>

            <button @click="handleAction" :disabled="workspace.myStatus === 1" :class="[
                buttonConfig.colorClass,
                'text-white text-sm font-bold py-2.5 px-5 rounded-lg transition-all duration-200 shadow-md flex items-center gap-2 group-hover:shadow-lg disabled:opacity-70'
            ]">
                {{ buttonConfig.text }}
                <span class="material-symbols-outlined text-sm">{{ buttonConfig.icon }}</span>
            </button>
        </div>
    </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Workspace } from '@/types';

interface Props {
    workspace: Workspace;
}
const props = defineProps<Props>();
const emit = defineEmits<{
    (e: 'enter' | 'apply', id: string | number, role?: string): void;
}>();

// 2. 버튼 설정 최적화 및 아이콘 동적 할당
const buttonConfig = computed(() => {
    type ButtonAction = 'enter' | 'apply';
    const configs = {
        0: {
            text: '참여 신청하기',
            action: 'apply' as ButtonAction,
            colorClass: 'bg-emerald-600 hover:bg-slate-900',
            icon: 'person_add',
            badgeClass: 'bg-emerald-50 text-emerald-700', // 신청 가능 상태 (초록톤)
            dotClass: 'bg-emerald-500'
        },
        1: {
            text: '승인 대기중',
            action: 'apply' as ButtonAction,
            colorClass: 'bg-slate-400 cursor-not-allowed',
            icon: 'pending',
            badgeClass: 'bg-slate-100 text-slate-600',    // 대기 상태 (회색톤)
            dotClass: 'bg-slate-400'
        },
        2: {
            text: '입장하기',
            action: 'enter' as ButtonAction,
            colorClass: 'bg-indigo-brand hover:bg-slate-900',
            icon: 'login',
            badgeClass: 'bg-indigo-50 text-indigo-700',   // 활동 중 상태 (보라톤)
            dotClass: 'bg-indigo-500'
        },
    };

    return configs[(props.workspace.myStatus || 0) as keyof typeof configs] ?? configs[0];
});

// 3. 상태 텍스트는 버튼 텍스트와 분리될 수 있으므로 별도 관리
const statusText = computed(() => {
    if (props.workspace.myStatus === 2) return '활동 중';
    if (props.workspace.myStatus === 1) return '대기 중';
    return '미가입';
});

// 4. 클릭 핸들러 (대기 상태일 때 로직 방지)
const handleAction = () => {
    if (props.workspace.myStatus === 1) return; // 대기 중일 때는 무시
    emit(buttonConfig.value.action, props.workspace.workspaceId, props.workspace.myRole || undefined);
};

// 테마 색상 맵
const themeClasses = {
    indigo: { bg: 'bg-indigo-50', border: 'border-indigo-100', icon: 'text-indigo-600', badge: 'bg-green-100 text-green-700', dot: 'bg-green-500' },
    blue: { bg: 'bg-blue-50', border: 'border-blue-100', icon: 'text-blue-600', badge: 'bg-blue-100 text-blue-700', dot: 'bg-blue-500' },
    amber: { bg: 'bg-amber-50', border: 'border-amber-100', icon: 'text-amber-600', badge: 'bg-amber-100 text-amber-700', dot: 'bg-amber-500' },
};

const theme = computed(() => {
    // themeColor가 string이면 as keyof로 단언하거나 기본값 처리 필요
    const color = 'indigo'
    return themeClasses[color] || themeClasses.indigo;
});
</script>


<style scoped>
.bg-indigo-brand {
    background-color: #4F46E5;
}

.hover\:bg-slate-900:hover {
    background-color: #0F172A;
}

/* 그림자 유틸리티 명확화 */
.shadow-md {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.group:hover .shadow-lg {
    box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.2), 0 4px 6px -2px rgba(79, 70, 229, 0.1);
}

.glass-panel {
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 1rem;
}
</style>
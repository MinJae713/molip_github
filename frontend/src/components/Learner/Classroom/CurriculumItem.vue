<template>
    <div class="group bg-white border rounded-xl p-5 transition-all flex flex-col sm:flex-row gap-4 relative overflow-hidden"
        :class="[
            isLive ? 'border-2 border-indigo-500/20 shadow-soft ring-1 ring-indigo-500/10' : 'border-slate-200 hover:border-indigo-300'
        ]">
        <div v-if="isLive" class="absolute top-0 right-0 p-3">
            <span class="relative flex h-3 w-3">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
            </span>
        </div>

        <div class="flex-none flex flex-col items-center justify-center w-12 pt-1 sm:pt-0">
            <div v-if="isLive"
                class="size-8 rounded-full bg-red-500 text-white flex items-center justify-center font-bold text-[10px] shadow-md animate-pulse">
                LIVE
            </div>
            <div v-else-if="isCompleted"
                class="size-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-sm">
                <span class="material-symbols-outlined text-[18px]">check</span>
            </div>
            <div v-else
                class="size-8 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center font-bold text-sm border border-slate-200">
                {{ String(index + 1).padStart(2, '0') }}
            </div>
            <div class="h-full w-0.5 bg-slate-100 my-2 hidden sm:block group-last:hidden"></div>
        </div>

        <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-bold uppercase tracking-wider"
                    :class="isLive ? 'text-red-500' : 'text-slate-400'">
                    SESSION {{ String(index + 1).padStart(2, '0') }}
                </span>
                <span class="px-1.5 py-0.5 text-[10px] rounded font-bold border" :class="typeBadgeClass">
                    {{ session.type === 'live' ? '실시간 강의' : '과제 제출' }}
                </span>
            </div>

            <h3 class="font-bold text-lg" :class="isCompleted ? 'text-slate-900' : 'text-slate-800'">
                {{ session.title }}
            </h3>

            <p v-if="session.description" class="text-sm text-slate-600 mt-1 mb-2">{{ session.description }}</p>

            <div class="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-slate-500">
                <div class="flex items-center gap-1" :class="{ 'text-slate-800 font-medium': isLive }">
                    <span class="material-symbols-outlined text-[16px]" :class="{ 'text-red-500': isLive }">
                        {{ isLive ? 'videocam' : (session.type === 'live' ? 'calendar_today' : 'event_upcoming') }}
                    </span>
                    {{ session.date }} {{ isLive ? '(진행 중)' : '' }}
                </div>
                <div v-if="session.duration" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">schedule</span> {{ session.duration }}
                </div>

                <div v-if="session.is_top3"
                    class="flex items-center gap-1 text-amber-700 font-bold bg-amber-50 px-2 py-0.5 rounded text-xs border border-amber-200">
                    <span class="material-symbols-outlined text-[15px] fill-current text-amber-500">emoji_events</span>
                    집중도 Top 3
                </div>
                <div v-if="session.quiz_score"
                    class="flex items-center gap-1 text-indigo-600 font-medium bg-indigo-50 px-2 py-0.5 rounded text-xs">
                    <span class="material-symbols-outlined text-[14px]">assignment_turned_in</span> 퀴즈 완료 ({{
                    session.quiz_score }}점)
                </div>
            </div>
        </div>

        <div class="flex items-center sm:self-center">
            <button v-if="isLive"
                class="w-full sm:w-auto px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-md transition-all flex items-center justify-center gap-2 group">
                <span class="material-symbols-outlined text-[18px]">login</span> 입장하기
                <span
                    class="material-symbols-outlined text-[16px] group-hover:translate-x-1 transition-transform">arrow_forward</span>
            </button>
            <button v-else-if="isCompleted"
                class="w-full sm:w-auto px-4 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-sm font-bold rounded-lg flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">{{ session.type === 'live' ? 'history' :
                    'visibility' }}</span>
                {{ session.type === 'live' ? '복습하기' : '제출 확인' }}
            </button>
            <button v-else
                class="w-full sm:w-auto px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">{{ session.type === 'live' ? 'calendar_today' :
                    'upload_file' }}</span>
                {{ session.type === 'live' ? '예정' : '과제 제출하기' }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
    session: any;
    index: number;
}>();

const isLive = computed(() => props.session.status === 'live');
const isCompleted = computed(() => props.session.status === 'completed');

const typeBadgeClass = computed(() => {
    if (props.session.status === 'live') return 'bg-red-50 text-red-600 border-red-100';
    if (props.session.type === 'live') return 'bg-indigo-50 text-indigo-600 border-indigo-100';
    return 'bg-blue-50 text-blue-600 border-blue-100';
});
</script>
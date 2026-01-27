<template>
    <div class="bg-white border border-slate-200 rounded-2xl flex flex-col shadow-sm shrink-0">
        <div class="p-3 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
            <h3 class="font-bold text-slate-800 text-xs flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600 text-[16px]">monitoring</span>
                나의 참여도 (My Engagement)
            </h3>
        </div>
        <div class="p-4 flex flex-col gap-4">
            <div class="flex items-center gap-3">
                <!-- Score Circle -->
                <div class="relative size-16 flex-shrink-0">
                    <svg class="size-full -rotate-90" viewBox="0 0 36 36">
                        <path class="text-slate-100"
                            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                            fill="none" stroke="currentColor" stroke-width="3"></path>
                        <path class="text-red-500"
                            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                            fill="none" stroke="currentColor" :stroke-dasharray="`${engagementScore}, 100`"
                            stroke-linecap="round" stroke-width="3"></path>
                    </svg>
                    <div class="absolute inset-0 flex flex-col items-center justify-center">
                        <span class="text-lg font-bold text-slate-800 leading-none">{{ engagementScore }}</span>
                        <span class="text-[8px] text-slate-400 font-bold">SCORE</span>
                    </div>
                </div>

                <!-- AI Sentiment -->
                <div class="flex-1 bg-indigo-50 border border-indigo-100 rounded-xl p-2.5 flex items-center gap-3">
                    <div class="flex-1">
                        <div class="text-[10px] text-indigo-600 font-bold leading-tight mb-1">실시간 감정 분석</div>
                        <div class="flex items-center gap-2">
                            <span class="text-2xl">{{ currentEmotion.emoji }}</span>
                            <div class="flex flex-col">
                                <span class="text-[11px] font-bold text-slate-800">{{ currentEmotion.label }}</span>
                                <span class="text-[9px] text-slate-500">AI 분석 결과</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Warning -->
            <div v-if="focusStatus.includes('낮음')"
                class="bg-red-50 rounded-lg p-2.5 border border-red-100 shadow-sm relative overflow-hidden flex items-start gap-2">
                <div class="absolute top-0 left-0 w-0.5 h-full bg-red-400"></div>
                <span class="material-symbols-outlined text-red-500 text-base shrink-0 mt-0.5">warning</span>
                <div class="flex flex-col">
                    <span class="text-[10px] font-bold text-red-600 mb-0.5">주의 필요</span>
                    <p class="text-[10px] text-red-600/90 leading-tight">집중도가 낮아지고 있습니다. 스트레칭이 필요합니다.</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    engagementScore: number;
    currentEmotion: { emoji: string; label: string };
    focusStatus: string;
}>();
</script>

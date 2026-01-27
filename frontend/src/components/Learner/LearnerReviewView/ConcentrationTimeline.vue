<template>
    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-600">monitoring</span>
                    집중도 타임라인
                </h2>
                <p class="text-xs text-slate-500 mt-1">실시간 세션 집중도 분석 데이터</p>
            </div>
            <div class="flex items-center gap-4 text-xs">
                <div class="flex items-center gap-1.5">
                    <span class="size-2.5 rounded-full bg-red-500/30 border border-red-500/50"></span>
                    <span class="text-slate-600 font-medium text-[11px]">데드존</span>
                </div>
                <div class="flex items-center gap-1.5">
                    <span class="size-2.5 rounded-full bg-indigo-600"></span>
                    <span class="text-slate-600 font-medium text-[11px]">나의 집중도</span>
                </div>
            </div>
        </div>
        <div class="relative w-full h-32">
            <div
                class="absolute left-0 top-0 bottom-6 w-8 flex flex-col justify-between text-[10px] text-slate-400 text-right pr-2">
                <span>100%</span>
                <span>50%</span>
                <span>0%</span>
            </div>
            <div class="absolute left-8 right-0 top-0 bottom-6 border-l border-b border-slate-100">
                <div class="absolute top-0 bottom-0 bg-red-500/10 border-x border-red-500/20 hover:bg-red-500/20 cursor-pointer transition-all flex flex-col items-center justify-start pt-1"
                    style="left: 28%; width: 12%;">
                    <span
                        class="text-[9px] font-bold text-red-500 bg-white px-1 rounded shadow-sm border border-red-500/20">데드존</span>
                </div>
                <div class="absolute top-0 bottom-0 bg-red-500/10 border-x border-red-500/20 hover:bg-red-500/20 cursor-pointer transition-all flex flex-col items-center justify-start pt-1"
                    style="left: 85%; width: 10%;">
                    <span
                        class="text-[9px] font-bold text-red-500 bg-white px-1 rounded shadow-sm border border-red-500/20">데드존</span>
                </div>
                <svg class="absolute inset-0 w-full h-full overflow-visible pointer-events-none"
                    preserveAspectRatio="none" viewBox="0 0 100 100">
                    <defs>
                        <linearGradient id="engagementGradient" x1="0" x2="0" y1="0" y2="1">
                            <stop offset="0%" stop-color="#4F46E5" stop-opacity="0.15"></stop>
                            <stop offset="100%" stop-color="#4F46E5" stop-opacity="0"></stop>
                        </linearGradient>
                    </defs>
                    <path d="M0,20 Q15,10 25,60 Q35,85 50,30 T80,25 T90,70 L100,30 L100,100 L0,100 Z"
                        fill="url(#engagementGradient)"></path>
                    <path class="text-indigo-600 drop-shadow-sm"
                        d="M0,20 Q15,10 25,60 Q35,85 50,30 T80,25 T90,70 L100,30" fill="none" stroke="currentColor"
                        stroke-linecap="round" stroke-width="2.5"></path>
                </svg>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
</script>
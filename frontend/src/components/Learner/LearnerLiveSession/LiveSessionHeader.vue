<template>
    <header
        class="flex items-center justify-between border-b border-slate-200 px-6 py-3 bg-white z-10 shadow-sm shrink-0">
        <div class="flex items-center gap-4">
            <div class="size-8 text-indigo-600">
                <span class="material-symbols-outlined text-3xl">school</span>
            </div>
            <div class="flex flex-col">
                <h2 class="font-bold text-slate-800 text-sm leading-tight">
                    {{ sessionTitle }}
                </h2>
                <p class="text-xs text-slate-500">{{ sessionSubtitle }}</p>
            </div>
        </div>

        <div class="flex items-center gap-4">
            <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 border border-slate-200">
                <div class="size-2 rounded-full bg-red-500 animate-pulse"></div>
                <span class="text-xs font-bold text-slate-700 font-mono tracking-wider">{{ formatTime(elapsedTime)
                    }}</span>
            </div>
            <div class="flex items-center gap-3 pl-4 border-l border-slate-200">
                <div class="flex flex-col items-end">
                    <span class="text-[10px] font-bold text-slate-400 leading-none mb-0.5">LEARNER</span>
                    <span class="text-xs font-bold text-slate-700 uppercase">Kim SSAFY</span>
                </div>
                <div class="size-8 rounded-full border-2 border-indigo-100 overflow-hidden shadow-sm">
                    <img class="size-full object-cover"
                        src="https://ui-avatars.com/api/?name=Student&background=4F46E5&color=fff" />
                </div>
            </div>
        </div>
    </header>
</template>

<script setup lang="ts">
defineProps<{
    sessionTitle: string;
    sessionSubtitle: string;
    elapsedTime: number;
}>();

const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};
</script>

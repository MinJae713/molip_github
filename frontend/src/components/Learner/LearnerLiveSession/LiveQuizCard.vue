<template>
    <div
        class="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm relative shrink-0 hover:border-indigo-300 transition-colors cursor-pointer">
        <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-indigo-600 to-cyan-400"></div>
        <div class="p-4 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="relative flex items-center justify-center size-9 rounded-full bg-pink-100 text-pink-500">
                    <span
                        class="absolute flex size-2.5 rounded-full bg-pink-500 animate-pulse top-0 right-0 border-2 border-white"></span>
                    <span class="material-symbols-outlined text-lg">quiz</span>
                </div>
                <div class="flex flex-col">
                    <h3 class="font-bold text-indigo-600 text-xs tracking-wide">{{ title }}</h3>
                    <p class="text-[10px] text-slate-500 truncate w-32">{{ subtitle }}</p>
                </div>
            </div>
            <div
                class="flex items-center gap-1 text-pink-500 font-mono text-xs font-bold bg-pink-50 px-2 py-1 rounded border border-pink-100">
                <span class="material-symbols-outlined text-[14px]">timer</span>
                <span>{{ timeRemaining }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    title: string;
    subtitle: string;
    timeRemaining: string;
}>();
</script>

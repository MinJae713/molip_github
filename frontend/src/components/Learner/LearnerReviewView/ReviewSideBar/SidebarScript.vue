<template>
    <div class="h-full overflow-y-auto custom-scrollbar p-4">
        <!-- Empty State -->
        <div v-if="!scriptList || scriptList.length === 0" 
             class="h-full flex flex-col items-center justify-center text-slate-400 gap-3">
            <span class="material-symbols-outlined text-4xl opacity-30">subject</span>
            <span class="text-sm font-medium">스크립트 데이터가 없습니다.</span>
        </div>

        <!-- Script List -->
        <div v-else class="space-y">
            <div v-for="(item, index) in scriptList" :key="index"
                 class="group flex gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer border border-transparent hover:border-slate-100"
                 @click="handleJump(item.timeline)">
                
                <!-- Timestamp -->
                <div class="flex-none pt-0.5">
                    <span class="inline-block px-2 py-0.5 bg-indigo-50 text-indigo-600 text-xs font-bold rounded group-hover:bg-indigo-100 transition-colors">
                        {{ formatTime(item.timeline) }}
                    </span>
                </div>

                <!-- Text -->
                <div class="flex-1 text-sm text-slate-700 leading-relaxed font-medium">
                    {{ item.message }}
                </div>
            </div>
            
            <div class="text-center py-4 text-xs text-slate-400">
                End of Script
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

interface ScriptItem {
    timeline: number; // seconds
    message: string;
}

const props = defineProps<{
    scriptList?: ScriptItem[];
}>();

import { onMounted, watch } from 'vue';

onMounted(() => {
    console.log("[SidebarScript] Mounted. scriptList:", props.scriptList);
});

watch(() => props.scriptList, (newVal) => {
    console.log("[SidebarScript] scriptList changed:", newVal);
}, { deep: true, immediate: true });

const emit = defineEmits(['jump-to-time']);

const handleJump = (time: number) => {
    console.log("Jumping to:", time);
    emit('jump-to-time', time);
};

const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 3px;
}
</style>
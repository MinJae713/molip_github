<template>
    <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-30">
        <div
            class="bg-white/90 backdrop-blur-md px-6 py-3 rounded-2xl flex items-center gap-5 shadow-2xl border border-white/40">
            <button @click="$emit('toggle-mic')" class="flex flex-col items-center gap-1 group">
                <div
                    :class="['size-10 rounded-xl flex items-center justify-center shadow-sm transition-all', isMicOn ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-500 group-hover:bg-slate-200']">
                    <span class="material-symbols-outlined text-[20px]">{{ isMicOn ? 'mic' : 'mic_off' }}</span>
                </div>
                <span class="text-[10px] font-medium text-slate-500">{{ isMicOn ? '마이크 끄기' : '마이크 켜기'
                }}</span>
            </button>

            <button @click="$emit('toggle-camera')" class="flex flex-col items-center gap-1 group">
                <div
                    :class="['size-10 rounded-xl flex items-center justify-center shadow-sm transition-all', isCameraOn ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-500 group-hover:bg-slate-200']">
                    <span class="material-symbols-outlined text-[20px]">{{ isCameraOn ? 'videocam' : 'videocam_off'
                    }}</span>
                </div>
                <span class="text-[10px] font-medium text-slate-500">{{ isCameraOn ? '카메라 켜기' : '카메라 끄기'
                }}</span>
            </button>
            <div class="w-px h-8 bg-slate-300 mx-1"></div>
            <button @click="$emit('raise-hand')" class="flex flex-col items-center gap-1 group">
                <div
                    :class="['size-10 rounded-xl flex items-center justify-center shadow-sm transition-all', isHandRaised ? 'bg-amber-100 text-amber-600 scale-110' : 'bg-slate-100 text-slate-500 group-hover:bg-slate-200']">
                    <span
                        :class="['material-symbols-outlined text-[20px]', isHandRaised && 'fill-current']">front_hand</span>
                </div>
                <span
                    :class="['text-[10px] font-medium', isHandRaised ? 'text-indigo-600' : 'text-slate-500']">손들기</span>
            </button>

            <button class="flex flex-col items-center gap-1 group">
                <div
                    class="size-10 rounded-xl bg-red-50 text-red-500 flex items-center justify-center shadow-sm group-hover:bg-red-500 group-hover:text-white transition-all">
                    <span class="material-symbols-outlined text-[20px]">logout</span>
                </div>
                <span class="text-[10px] font-medium text-slate-400 group-hover:text-red-500">나가기</span>
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    isMicOn: boolean;
    isCameraOn: boolean;
    isHandRaised: boolean;
}>();

defineEmits<{
    (e: 'toggle-mic'): void;
    (e: 'toggle-camera'): void;
    (e: 'raise-hand'): void;
}>();
</script>

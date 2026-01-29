<template>
    <div
        class="flex-1 flex flex-col relative rounded-2xl overflow-hidden border border-slate-200 shadow-xl bg-slate-900 group/video">
        <!-- 강의 화면/강사 화면공유 부분 -->
        <div class="absolute inset-0 flex items-center justify-center bg-slate-800">
            <video ref="mainVideoRef" autoplay playsinline class="w-full h-full object-contain"></video>
            <div v-if="!mainStream" class="absolute inset-0 flex items-center justify-center text-slate-500">
                <p>강의 화면 대기 중...</p>
            </div>
        </div>

        <!-- ai 집중도 정보 -->
        <div class="absolute top-6 left-6 flex items-center gap-3">
            <div
                class="bg-white/90 backdrop-blur-md px-4 py-2 rounded-full flex items-center gap-3 border-l-4 border-l-red-500 shadow-lg">
                <div class="flex flex-col">
                    <span class="text-[9px] font-bold text-slate-400 uppercase leading-none mb-0.5">Focus
                        Tracking</span>
                    <span class="text-xs font-black text-slate-800">{{ focusStatus }}</span>
                </div>
                <span class="material-symbols-outlined text-slate-400 text-lg ml-1">visibility</span>
            </div>
        </div>

        <!-- 경고 알람 -->
        <div v-if="showWarning" class="absolute inset-0 z-50 flex items-center justify-center pointer-events-none">
            <div
                class="bg-white/95 backdrop-blur-md rounded-2xl shadow-2xl p-6 border-l-4 border-red-500 flex items-center gap-5 pointer-events-auto max-w-md animate-bounce ring-1 ring-black/5">
                <div class="size-12 rounded-full bg-red-100 text-red-500 flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-2xl">error</span>
                </div>
                <div class="flex-1">
                    <h4 class="font-bold text-slate-900 text-sm mb-1">집중도 저하 감지!</h4>
                    <p class="text-xs text-slate-600 leading-relaxed">자세가 흐트러지거나 화면을 벗어났습니다. 화면을 주시해 주세요.</p>
                </div>
                <button @click="$emit('close-warning')"
                    class="absolute top-3 right-3 text-slate-300 hover:text-slate-500 p-1">
                    <span class="material-symbols-outlined text-lg">close</span>
                </button>
            </div>
        </div>

        <!-- 내 카메라 pip -->
        <div
            class="absolute top-6 right-6 w-48 aspect-video rounded-xl bg-slate-800 border-2 border-white/20 shadow-2xl overflow-hidden group/pip hover:scale-105 transition-transform duration-300">
            <div class="absolute top-6 right-6 w-48 aspect-video ...">
                <video ref="pipVideoRef" autoplay playsinline muted class="w-full h-full object-cover"></video>
                <span class="absolute bottom-2 left-2 text-[10px] text-white bg-black/40 px-1">
                    {{ userRole === 'TRAINER' ? 'Professor (Me)' : 'Me' }}
                </span>
            </div>
        </div>

        <slot name="controls"></slot>
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
    focusStatus: string;
    showWarning: boolean;
    mainStream: MediaStream | null;
    pipStream: MediaStream | null;
    userRole?: string | null;
}>();

defineEmits<{
    (e: 'close-warning'): void;
}>();

const mainVideoRef = ref<HTMLVideoElement | null>(null);
const pipVideoRef = ref<HTMLVideoElement | null>(null);

// Main Stream Binding
watch(
    () => props.mainStream,
    (newStream) => {
        if (mainVideoRef.value) {
            mainVideoRef.value.srcObject = newStream;
        }
    },
    { immediate: true }
);

// PIP Stream Binding
watch(
    () => props.pipStream,
    (newStream) => {
        if (pipVideoRef.value) {
            pipVideoRef.value.srcObject = newStream;
        }
    },
    { immediate: true }
);
</script>

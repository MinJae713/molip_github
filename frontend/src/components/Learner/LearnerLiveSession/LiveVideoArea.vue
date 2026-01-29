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

        <!-- 내 카메라 pip (Draggable) -->
        <div ref="pipContainerRef"
            class="absolute w-48 aspect-video rounded-xl bg-slate-800 border-2 border-white/20 shadow-2xl overflow-hidden group/pip cursor-move active:cursor-grabbing hover:scale-105 transition-transform duration-300 z-40"
            :style="{ top: `${pipPos.y}px`, left: `${pipPos.x}px` }" @mousedown="startDrag">
            <div class="relative w-full h-full pointer-events-none">
                <!-- localStream이 우선, 없으면 pipStream (기존 로직) -->
                <video ref="pipVideoRef" autoplay playsinline muted
                    class="w-full h-full object-cover transform scale-x-[-1]"></video>
                <span class="absolute bottom-2 left-2 text-[10px] text-white bg-black/40 px-1 rounded">
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
    localStream?: MediaStream | null;
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
// PIP Stream Binding (LocalStream 우선)
watch(
    () => [props.pipStream, props.localStream],
    ([newPip, newLocal]) => {
        if (pipVideoRef.value) {
            // 로컬 스트림이 있으면 우선 표시 (Self-View)
            if (newLocal) {
                pipVideoRef.value.srcObject = newLocal;
            } else if (newPip) {
                pipVideoRef.value.srcObject = newPip;
            } else {
                pipVideoRef.value.srcObject = null;
            }
        }
    },
    { immediate: true }
);

// --- Draggable Logic ---
const pipContainerRef = ref<HTMLElement | null>(null);
const isDragging = ref(false);
const pipPos = ref({ x: window.innerWidth - 240, y: 80 }); // 초기 위치 (우측 상단, top-6 right-6 등 CSS와 일치시키거나 적절히 조정)
const dragOffset = ref({ x: 0, y: 0 });

function startDrag(e: MouseEvent) {
    if (!pipContainerRef.value) return;
    isDragging.value = true;
    dragOffset.value = {
        x: e.clientX - pipPos.value.x,
        y: e.clientY - pipPos.value.y
    };

    // 전역 이벤트 등록 (드래그 중 마우스가 요소 밖으로 나가도 끊기지 않게)
    window.addEventListener('mousemove', onDrag);
    window.addEventListener('mouseup', stopDrag);
}

function onDrag(e: MouseEvent) {
    if (!isDragging.value) return;

    // 새 위치 계산
    let newX = e.clientX - dragOffset.value.x;
    let newY = e.clientY - dragOffset.value.y;

    // 경계 체크 (화면 밖으로 나가지 않게)
    // Container video size (VideoArea 전체)를 기준으로 하면 좋지만,
    // 여기서는 window 기준으로 간단히 제한 (전체화면 가정)
    // 실제 컴포넌트 크기 내에서만 움직이게 하려면, 부모 요소의 getBoundingClientRect() 참조 필요

    // 임시: 안전하게 화면 내 부유
    const maxX = window.innerWidth - (pipContainerRef.value?.offsetWidth || 192);
    const maxY = window.innerHeight - (pipContainerRef.value?.offsetHeight || 108);

    if (newX < 0) newX = 0;
    if (newY < 0) newY = 0;
    if (newX > maxX) newX = maxX;
    if (newY > maxY) newY = maxY;

    pipPos.value = { x: newX, y: newY };
}

function stopDrag() {
    isDragging.value = false;
    window.removeEventListener('mousemove', onDrag);
    window.removeEventListener('mouseup', stopDrag);
}
</script>

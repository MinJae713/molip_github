<template>
    <div class="flex-none h-full flex flex-col justify-center">
        <!-- 헤더 및 범례 -->
        <div class="flex flex-col gap-2 mb-6">
            <h3 class="flex items-center gap-2 font-bold text-slate-800 text-lg">
                <div class="w-8 h-8 rounded-full bg-purple-500 flex items-center justify-center text-white shadow-sm">
                    <span class="material-symbols-outlined text-[20px]">face</span>
                </div>
                개인감정 타임라인
            </h3>
            <!-- 범례 (2줄로 구성) -->
            <div class="flex flex-wrap gap-2 text-[10px] sm:text-xs">
                <div class="flex items-center gap-1.5 px-2 py-1 bg-emerald-50 rounded-full border border-emerald-100">
                    <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                    <span class="text-slate-600 font-medium">행복</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-yellow-50 rounded-full border border-yellow-100">
                    <span class="w-2 h-2 rounded-full bg-yellow-400"></span>
                    <span class="text-slate-600 font-medium">놀람</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-purple-50 rounded-full border border-purple-100">
                    <span class="w-2 h-2 rounded-full bg-purple-400"></span>
                    <span class="text-slate-600 font-medium">공포</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-lime-50 rounded-full border border-lime-100">
                    <span class="w-2 h-2 rounded-full bg-lime-400"></span>
                    <span class="text-slate-600 font-medium">혐오</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-blue-50 rounded-full border border-blue-100">
                    <span class="w-2 h-2 rounded-full bg-blue-400"></span>
                    <span class="text-slate-600 font-medium">슬픔</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-red-50 rounded-full border border-red-100">
                    <span class="w-2 h-2 rounded-full bg-red-400"></span>
                    <span class="text-slate-600 font-medium">분노</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-slate-100 rounded-full border border-slate-200">
                    <span class="w-2 h-2 rounded-full bg-slate-400"></span>
                    <span class="text-slate-600 font-medium">중립</span>
                </div>
            </div>
        </div>

        <!-- 타임라인 바 -->
        <div class="h-24 w-full bg-slate-50 rounded-2xl overflow-hidden flex relative border border-slate-200 shadow-inner">
            <div v-for="(segment, index) in emotionSegments" :key="index"
                 class="h-full flex items-center justify-center transition-all hover:brightness-95 relative group cursor-default"
                 :class="getEmotionColor(segment.type)"
                 :style="{ width: segment.width + '%' }">
                <span class="text-2xl sm:text-3xl drop-shadow-sm transform group-hover:scale-125 transition-transform duration-200 select-none">
                    {{ getEmotionEmoji(segment.type) }}
                </span>
                <!-- 툴팁 -->
                <div class="absolute opacity-0 group-hover:opacity-100 bottom-full mb-3 bg-slate-800 text-white text-xs px-3 py-2 rounded-lg shadow-xl pointer-events-none transition-all transform translate-y-2 group-hover:translate-y-0 whitespace-nowrap z-50">
                    <div class="font-bold mb-0.5 text-center">{{ getEmotionLabel(segment.type) }}</div>
                    <div class="text-slate-300 text-[10px]">{{ formatTime(segment.start) }} ~ {{ formatTime(segment.end) }}</div>
                    <!-- 화살표 -->
                    <div class="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-slate-800"></div>
                </div>
            </div>
        </div>
        
        <!-- 하단 시간 표시 -->
        <div class="flex justify-between mt-2 text-xs text-slate-400 font-medium px-1 select-none">
            <span>00:00</span>
            <span>{{ formatTime(totalDuration * 0.25) }}</span>
            <span>{{ formatTime(totalDuration * 0.5) }}</span>
            <span>{{ formatTime(totalDuration * 0.75) }}</span>
            <span>{{ formatTime(totalDuration) }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface EmotionSegment {
    start: number;
    end: number;
    type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral';
}

const props = withDefaults(defineProps<{
    duration?: number; // 분 단위
    emotionData?: EmotionSegment[];
}>(), {
    duration: 60,
    emotionData: () => []
});

const defaultEmotionData: EmotionSegment[] = [
    { start: 0, end: 10, type: 'neutral' },
    { start: 10, end: 20, type: 'happy' },
    { start: 20, end: 25, type: 'surprise' },
    { start: 25, end: 35, type: 'neutral' },
    { start: 35, end: 40, type: 'sad' },
    { start: 40, end: 50, type: 'anger' },
    { start: 50, end: 55, type: 'fear' },
    { start: 55, end: 60, type: 'disgust' }
];

const activeEmotionData = computed(() => {
    return (props.emotionData && props.emotionData.length > 0) ? props.emotionData : defaultEmotionData;
});

const totalDuration = computed(() => props.duration || 60);

const emotionSegments = computed(() => {
    return activeEmotionData.value.map(seg => ({
        ...seg,
        width: ((seg.end - seg.start) / totalDuration.value) * 100
    }));
});

function getEmotionColor(type: string) {
    switch (type) {
        case 'happy': return 'bg-emerald-100/80 border-r border-emerald-200';
        case 'surprise': return 'bg-yellow-100/80 border-r border-yellow-200';
        case 'fear': return 'bg-purple-100/80 border-r border-purple-200';
        case 'disgust': return 'bg-lime-100/80 border-r border-lime-200';
        case 'sad': return 'bg-blue-100/80 border-r border-blue-200';
        case 'anger': return 'bg-red-100/80 border-r border-red-200';
        case 'neutral': return 'bg-slate-100/80 border-r border-slate-200';
        default: return 'bg-slate-100';
    }
}

function getEmotionEmoji(type: string) {
    switch (type) {
        case 'happy': return '😊';
        case 'surprise': return '😲';
        case 'fear': return '😨';
        case 'disgust': return '🤢';
        case 'sad': return '😢';
        case 'anger': return '😡';
        case 'neutral': return '😐';
        default: return '😐';
    }
}

function getEmotionLabel(type: string) {
    switch (type) {
        case 'happy': return '행복';
        case 'surprise': return '놀람';
        case 'fear': return '공포';
        case 'disgust': return '혐오';
        case 'sad': return '슬픔';
        case 'anger': return '분노';
        case 'neutral': return '중립';
        default: return '알 수 없음';
    }
}

function formatTime(totalMinutes: number) {
    const mm = Math.floor(totalMinutes);
    const ss = Math.floor((totalMinutes - mm) * 60);
    return `${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}`;
}
</script>

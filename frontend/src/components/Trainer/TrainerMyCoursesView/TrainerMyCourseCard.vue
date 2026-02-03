<template>
    <div
        class="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-xl hover:border-indigo-300 transition-all duration-500 flex flex-col h-full relative overflow-hidden">
        <!-- 상단 강조 그라데이션 바 -->
        <div class="h-1.5 w-full bg-gradient-to-r from-indigo-600 via-indigo-500 to-cyan-400"></div>

        <div class="p-6 flex-1 flex flex-col">
            <!-- 헤더: 상태 표시 태그 + 추가 작업 버튼 -->
            <div class="flex justify-between items-start mb-6">
                <div :class="statusClass"
                    class="px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-widest border shadow-sm transition-all duration-300">
                    {{ statusLabel }}
                </div>
            </div>

            <!-- 강의 제목 -->
            <div class="mb-6">
                <h3
                    class="text-xl font-bold text-slate-900 leading-tight group-hover:text-indigo-600 transition-colors duration-300">
                    {{ displayCourse('title') }}
                </h3>
            </div>

            <!-- 주요 통계 정보 (수강생, 세션 수) -->
            <div class="grid grid-cols-2 gap-4 mb-6">
                <!-- 수강생 수 -->
                <div
                    class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 border border-slate-100 group-hover:bg-white group-hover:border-indigo-100 transition-colors duration-300">
                    <div class="w-10 h-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center">
                        <span class="material-symbols-outlined text-xl">group</span>
                    </div>
                    <div>
                        <div class="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">수강생</div>
                        <div class="text-base font-bold text-slate-900">{{ displayCourse('learnerCount') || 0 }}명</div>
                    </div>
                </div>
                <!-- 총 세션 수 -->
                <div
                    class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 border border-slate-100 group-hover:bg-white group-hover:border-indigo-100 transition-colors duration-300">
                    <div class="w-10 h-10 rounded-lg bg-cyan-100 text-cyan-600 flex items-center justify-center">
                        <span class="material-symbols-outlined text-xl">menu_book</span>
                    </div>
                    <div>
                        <div class="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">세션</div>
                        <div class="text-base font-bold text-slate-900">{{ displayCourse('totalSessionCount') || 0 }}개
                        </div>
                    </div>
                </div>
            </div>

            <!-- 진척도 표시 영역 -->
            <div class="mt-auto space-y-4">
                <div>
                    <div class="flex justify-between items-end mb-2">
                        <span class="text-xs font-bold text-slate-500">평균 진도율</span>
                        <span class="text-lg font-black text-indigo-600">
                            {{ formattedProgress }}%
                        </span>
                    </div>
                    <!-- 프로그레스 바 -->
                    <div class="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden shadow-inner">
                        <div class="h-full rounded-full bg-gradient-to-r from-indigo-500 to-indigo-600 shadow-sm transition-all duration-1000 ease-out relative"
                            :style="{ width: `${formattedProgress}%` }">
                            <!-- 로딩 효과 -->
                            <div class="absolute inset-0 bg-white/20 progress-shimmer"></div>
                        </div>
                    </div>
                </div>

                <!-- 강의실 관리 버튼 -->
                <button
                    class="w-full py-3.5 px-4 bg-slate-500 hover:bg-slate-600 text-white rounded-xl font-bold text-sm transition-all duration-300 flex items-center justify-center gap-2 group/btn shadow-lg hover:shadow-slate-200"
                    @click.stop="$emit('enter-course', displayCourse('courseId'))">
                    <span>과정 관리</span>
                    <span
                        class="material-symbols-outlined text-lg transition-transform duration-500 group-hover/btn:rotate-90">settings</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TrainerCourse } from '@/types';

// Props 정의: 강사용 강의 정보
interface Props {
    course: TrainerCourse;
}

const props = defineProps<Props>();

// 컴포넌트 이벤트 정의
defineEmits<{
    (e: 'enter-course', courseId: string | number): void; // 강의실 입장(관리) 이벤트
    (e: 'enroll-course', courseId: string | number): void; // 수강 신청 이벤트 (강사 뷰에서는 미사용)
}>();

/**
 * 강의 객체의 필드 값을 안전하게 반환하는 헬퍼 함수
 */
const displayCourse = (field: keyof TrainerCourse) => {
    return (props.course as any)[field];
};

/**
 * 평균 진도율 포맷팅 (null인 경우 0으로 처리)
 */
const formattedProgress = computed(() => {
    const rate = displayCourse('averageProgressRate');
    return rate === null || rate === undefined ? 0 : Math.round(rate);
});

/**
 * 현재 코스 상태에 따른 한글 레이블 반환
 */
const statusLabel = computed(() => {
    const status = displayCourse('courseStatus');
    switch (status) {
        case 'PROCEED': return '진행 중';
        case 'RECRUIT': return '모집 중';
        case 'END': return '종료됨';
        default: return status || '확인 필요';
    }
});

/**
 * 현재 코스 상태에 따른 스타일 클래스 반환
 */
const statusClass = computed(() => {
    const status = displayCourse('courseStatus');
    switch (status) {
        case 'PROCEED': return 'bg-emerald-50 text-emerald-600 border-emerald-100';
        case 'RECRUIT': return 'bg-indigo-50 text-indigo-600 border-indigo-100';
        case 'END': return 'bg-slate-50 text-slate-500 border-slate-200';
        default: return 'bg-slate-50 text-slate-500 border-slate-200';
    }
});
</script>

<style scoped>
/* 프로그레스 바 쉬머(반짝임) 애니메이션 효과 */
.progress-shimmer {
    background: linear-gradient(90deg,
            rgba(255, 255, 255, 0) 0%,
            rgba(255, 255, 255, 0.4) 50%,
            rgba(255, 255, 255, 0) 100%);
    background-size: 200% 100%;
    animation: shimmer 2s infinite linear;
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }

    100% {
        background-position: 200% 0;
    }
}

/* 카드 커스텀 그림자 효과 */
.shadow-card {
    box-shadow: 0 10px 30px -10px rgba(79, 70, 229, 0.1);
}

/* 스트라이프 패턴 배경 (필요 시 사용) */
.progress-bar-striped {
    background-image: linear-gradient(45deg, rgba(255, 255, 255, .15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, .15) 50%, rgba(255, 255, 255, .15) 75%, transparent 75%, transparent);
    background-size: 1rem 1rem;
}
</style>

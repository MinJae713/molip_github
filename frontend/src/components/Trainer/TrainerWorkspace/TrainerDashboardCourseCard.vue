<template>
    <article
        class="bg-white border border-slate-200 rounded-xl p-5 hover:border-indigo-300 hover:shadow-md transition-all group"
        v-bind:class="{ 'hover:shadow-sm opacity-75 hover:opacity-100': course.status === 'FINISH' }">
        <div class="flex gap-5">
            <div class="flex-1">
                <!-- 상태 태그 영역 -->
                <div class="flex items-center gap-2 mb-2">
                    <!-- 상태 배지 -->
                    <span v-if="course.status === 'PROCEED'"
                        class="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-[10px] font-bold rounded border border-indigo-100 uppercase tracking-wide">
                        진행 중
                    </span>
                    <span v-else-if="course.status === 'PREPARING'"
                        class="px-2 py-0.5 bg-emerald-50 text-emerald-600 text-[10px] font-bold rounded border border-emerald-100 uppercase tracking-wide">
                        준비중
                    </span>
                    <span v-else-if="course.status === 'FINISH'"
                        class="px-2 py-0.5 bg-slate-100 text-slate-600 text-[10px] font-bold rounded border border-slate-200 uppercase tracking-wide">
                        종료됨
                    </span>
                    <!-- 부가 정보 -->
                    <span v-if="course.status === 'PROCEED'"
                        class="text-xs font-medium text-slate-400 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">people</span> {{ course.learnersCount || 0
                        }}명 수강 중
                    </span>
                    <span v-else-if="course.status === 'PREPARING'" class="text-xs font-medium text-slate-400">
                        시작일: {{ course.startDate }}
                    </span>
                </div>
                <!-- 제목 및 설명 -->
                <h3 class="font-bold text-lg mb-1 group-hover:text-indigo-600 transition-colors"
                    v-bind:class="course.status === 'FINISH' ? 'text-slate-700' : 'text-slate-900'">
                    {{ course.title }}
                </h3>
                <p class="text-sm text-slate-500 line-clamp-2 mb-4">{{ course.description }}</p>
                <!-- 진도율 / 모집률 영역 -->
                <div class="space-y-2">
                    <div class="flex justify-between text-xs font-bold">
                        <!-- 진행 중 -->
                        <span v-if="course.status === 'PROCEED'" class="text-slate-500">
                            평균 진도율 {{ course.progressRate || 0 }}%
                        </span>
                        <!-- 모집 중 -->
                        <span v-else-if="course.status === 'PREPARING'" class="text-emerald-600">
                            모집률 {{ calculateRecruitmentRate() }}%
                        </span>
                        <!-- 종료됨 -->
                        <span v-else-if="course.status === 'FINISH'" class="text-slate-500">
                            최종 수료율 {{ course.completionRate || 0 }}%
                        </span>
                    </div>
                    <!-- 프로그레스 바 -->
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                        <div class="h-full rounded-full" v-bind:class="{
                            'bg-indigo-500': course.status === 'PROCEED',
                            'bg-emerald-500': course.status === 'PREPARING',
                            'bg-slate-400': course.status === 'FINISH'
                        }" v-bind:style="{ width: getProgressWidth() + '%' }">
                        </div>
                    </div>
                </div>
            </div>
            <!-- 버튼 영역 -->
            <div class="flex flex-col justify-start items-end gap-2 min-w-[110px]">
                <!-- 진행 중 / 모집 중 -->
                <template v-if="course.status === 'PROCEED' || course.status === 'PREPARING'">
                    <button v-if="course.isOwn === true" v-on:click="handleManageCourse"
                        class="w-full py-2 px-3 bg-white border border-slate-200 hover:bg-slate-50 text-slate-600 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 group/btn">
                        <span
                            class="material-symbols-outlined text-[16px] text-slate-400 group-hover/btn:text-indigo-500">settings</span>
                        과정 관리
                    </button>
                    <button v-on:click="handleOpenCurriculum"
                        class="w-full py-2 px-3 bg-white border border-slate-200 hover:bg-slate-50 text-slate-600 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 group/btn">
                        <span
                            class="material-symbols-outlined text-[16px] text-slate-400 group-hover/btn:text-indigo-500">menu_book</span>
                        커리큘럼 확인
                    </button>
                </template>
                <!-- 종료됨 -->
                <template v-else-if="course.status === 'FINISH'">
                    <button v-on:click="handleViewHistory"
                        class="w-full py-2 px-3 bg-white border border-slate-200 hover:bg-slate-50 text-slate-600 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 group/btn">
                        <span
                            class="material-symbols-outlined text-[16px] text-slate-400 group-hover/btn:text-indigo-500">history</span>
                        기록 보기
                    </button>
                    <button v-on:click="handleOpenCurriculum"
                        class="w-full py-2 px-3 bg-white border border-slate-200 hover:bg-slate-50 text-slate-600 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 group/btn">
                        <span
                            class="material-symbols-outlined text-[16px] text-slate-400 group-hover/btn:text-indigo-500">menu_book</span>
                        커리큘럼 확인
                    </button>
                </template>
            </div>
        </div>
    </article>
</template>
<script setup lang="ts">
import { computed } from 'vue';
interface Props {
    course: any;
}
const props = defineProps<Props>();
const emit = defineEmits<{
    (e: 'manage-course', courseId: string | number): void;
    (e: 'open-curriculum', courseId: string | number): void;
    (e: 'view-history', courseId: string | number): void;
}>();
// 모집률 계산
const calculateRecruitmentRate = () => {
    if (props.course.status === 'INVITE' && props.course.maxLearners) {
        const current = props.course.currentLearners || 0;
        const max = props.course.maxLearners;
        return Math.round((current / max) * 100);
    }
    return 0;
};
// 프로그레스 바 너비 계산
const getProgressWidth = () => {
    if (props.course.status === 'PROCEED') {
        return props.course.progressRate || 0;
    } else if (props.course.status === 'INVITE') {
        return calculateRecruitmentRate();
    } else if (props.course.status === 'FINISH') {
        return props.course.completionRate || 0;
    }
    return 0;
};
// 이벤트 핸들러
const handleManageCourse = () => {
    emit('manage-course', props.course.courseId);
};
const handleOpenCurriculum = () => {
    emit('open-curriculum', props.course.courseId);
};
const handleViewHistory = () => {
    emit('view-history', props.course.courseId);
};
</script>
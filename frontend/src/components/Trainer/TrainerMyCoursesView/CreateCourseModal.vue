<template>
    <div v-if="isOpen" class="fixed inset-0 z-[100] overflow-y-auto" aria-labelledby="modal-title" role="dialog"
        aria-modal="true">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>

        <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
            <div
                class="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-modal transition-all sm:my-8 sm:w-full sm:max-w-4xl border border-slate-200/50 flex flex-col max-h-[92vh]">

                <!-- Header -->
                <div
                    class="px-8 py-5 border-b border-slate-100 bg-white flex items-center justify-between sticky top-0 z-10 shrink-0">
                    <div>
                        <h2 class="text-xl font-bold text-slate-900" id="modal-title">새 교육 과정 만들기</h2>
                        <p class="text-sm text-slate-500 mt-0.5">AI 분석 설정 및 수료 조건을 정의합니다.</p>
                    </div>
                    <button @click="$emit('close')"
                        class="text-slate-400 hover:text-slate-600 transition-colors rounded-full p-1 hover:bg-slate-100"
                        type="button">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>

                <!-- Body (Scrollable) -->
                <div class="flex-1 overflow-y-auto bg-slate-50/50">
                    <!-- Step Indicator -->
                    <CreateCourseStepIndicator :currentStep="currentStep" :steps="steps" />
                    <!-- Step Content -->
                    <div class="px-8 pb-8">
                        <KeepAlive>
                            <component :is="currentStepComponent" v-model="form" />
                        </KeepAlive>
                    </div>
                </div>
                <!-- Footer (Sticky) -->
                <div
                    class="px-8 py-5 border-t border-slate-100 bg-slate-50 flex items-center justify-between sticky bottom-0 z-10 shrink-0">
                    <div class="flex items-center gap-3">
                        <button v-if="currentStep > 1" @click="prevStep"
                            class="px-5 py-2.5 text-slate-600 text-sm font-bold hover:text-slate-800 transition-colors">
                            이전 단계
                        </button>

                        <button v-if="currentStep < totalSteps" @click="nextStep"
                            class="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-bold shadow-md shadow-indigo-200 transition-colors">
                            다음 단계
                        </button>
                        <button v-else @click="handleSubmit" :disabled="isSubmitting"
                            class="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-bold shadow-md shadow-indigo-200 transition-all flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed">
                            <span v-if="isSubmitting"
                                class="animate-spin material-symbols-outlined text-[18px]">progress_activity</span>
                            <span v-else class="material-symbols-outlined text-[18px]">check</span>
                            <span>{{ isSubmitting ? '처리 중...' : '설정 완료 및 과정 생성' }}</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue';
import { useTrainerCourseStore } from '@/stores/trainerCourse';
import type { CreateCourse } from '@/types';

import CreateCourseStepIndicator from './CreateCourseStepIndicator.vue';
import Step1BasicInfo from './steps/CreateCourseStep1BasicInfo.vue';
import Step2Schedule from './steps/CreateCourseStep2Schedule.vue';
import Step3AIAnalysis from './steps/CreateCourseStep3AIAnalysis.vue';
import Step4Completion from './steps/CreateCourseStep4Completion.vue';

const props = defineProps<{
    isOpen: boolean;
    workspaceId: string | number;
    userId: string | number;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'created'): void;
}>();

const trainerCourseStore = useTrainerCourseStore();

// 상태 관리 변수들
const currentStep = ref(1);
const steps = ['기본 정보', '운영 일정', 'AI 분석', '수료 기준'];
const totalSteps = steps.length;
const isSubmitting = ref(false);

//모달 이동 제어
const currentStepComponent = computed(() => {
    switch (currentStep.value) {
        case 1: return Step1BasicInfo;
        case 2: return Step2Schedule;
        case 3: return Step3AIAnalysis;
        case 4: return Step4Completion;
        default: return Step1BasicInfo;
    }
});

// 폼 데이터
const form = reactive({
    // workspaceId는 별도로 props로 받음
    title: '',
    description: '',
    category: '',
    subCategory: '',
    maxLearner: 0,
    // Schedule
    period: '',
    isEngageCheck: false,
    isSentimentAnalysis: false,
    attendance: 0,
    engagement: 0,
    exitQuiz: 0,
});

// Reset form when modal opens
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        currentStep.value = 1;
        // Reset form logic if needed, or keep previous state for draft
    }
});

const nextStep = () => {
    if (currentStep.value < totalSteps) {
        // Validation logic can be added here
        if (currentStep.value === 1 && !form.title) {
            alert('과정명을 입력해주세요.');
            return;
        }
        currentStep.value++;
    }
};

const prevStep = () => {
    if (currentStep.value > 1) {
        currentStep.value--;
    }
};

const handleSubmit = async () => {
    isSubmitting.value = true;
    console.log(form);
    try {
        const payload = {
            ...form,
            workspaceId: String(props.workspaceId) as any,
        };
        const success = await trainerCourseStore.createCourse(payload);
        if (success) {
            emit('created');
            emit('close');
            // Form reset
            Object.assign(form, {
                title: '', description: '', category: '', sub_category: '', tag: '',
                period: '', duration: '', total_lessons: 0,
                ai_engagement_check: true, ai_sentiment_analysis: false,
                completion_attendance_rate: 80, use_engagement_criteria: true, completion_engagement_score: 70, use_quiz_criteria: false, completion_quiz_score: 60
            });
            currentStep.value = 1;
        } else {
            alert('과정 생성에 실패했습니다.');
        }
    } catch (e) {
        console.error(e);
        alert('오류가 발생했습니다.');
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<style scoped>
/* Scoped styles mainly handled by tailwind classes */
</style>

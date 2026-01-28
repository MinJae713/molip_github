<template>
    <div class="space-y-4">
        <!-- 교육 과정 수료 조건 설정 -->
        <div
            class="bg-white rounded-xl p-4 border border-indigo-100 shadow-sm relative overflow-hidden ring-1 ring-indigo-500/20">
            <div class="absolute top-0 left-0 w-1 h-full bg-indigo-500"></div>
            <div class="flex items-center gap-2 mb-3">
                <span class="bg-indigo-600 text-white text-[10px] font-bold px-1.5 py-0.5 rounded">STEP 4</span>
                <h3 class="font-bold text-base text-slate-800">교육 과정 수료 조건 설정</h3>
            </div>

            <div class="space-y-2.5">
                <!-- Attendance -->
                <div
                    class="bg-slate-50 rounded-lg border border-slate-200 p-3 flex flex-col sm:flex-row sm:items-center gap-3 transition-shadow hover:shadow-md hover:border-indigo-200 group">
                    <div class="flex items-center gap-3 flex-1">
                        <div
                            class="size-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center shrink-0 shadow-sm">
                            <span class="material-symbols-outlined text-xl">event_available</span>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h4 class="font-bold text-slate-900 text-sm">필수 출석률 (Attendance)</h4>
                                <span
                                    class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-700 border border-indigo-200">필수</span>
                            </div>
                            <p class="text-[11px] text-slate-500 mt-0.5">전체 과정의 최소 참여 비율 기준</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-2 pl-12 sm:pl-0 w-full sm:w-auto justify-end">
                        <div class="relative w-24">
                            <input :value="modelValue.completion_attendance_rate"
                                @input="updateField('completion_attendance_rate', Number(($event.target as HTMLInputElement).value))"
                                class="w-full rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors font-bold text-indigo-700 text-right pr-6 bg-white shadow-sm border-slate-300 border"
                                type="number" />
                            <span class="absolute right-2 top-2 text-slate-400 font-bold text-xs">%</span>
                        </div>
                        <span class="text-xs font-bold text-slate-700 whitespace-nowrap min-w-[2rem]">이상</span>
                    </div>
                </div>

                <!-- Engagement -->
                <div
                    class="bg-slate-50 rounded-lg border border-slate-200 p-3 flex flex-col sm:flex-row sm:items-center gap-3 transition-shadow hover:shadow-md hover:border-indigo-200 group">
                    <div class="flex items-center gap-3 flex-1">
                        <div
                            class="size-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center shrink-0 shadow-sm">
                            <span class="material-symbols-outlined text-xl">visibility</span>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h4 class="font-bold text-slate-900 text-sm">평균 집중도 (Engagement)</h4>
                                <span
                                    class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-slate-200 text-slate-600 border border-slate-300">선택</span>
                            </div>
                            <p class="text-[11px] text-slate-500 mt-0.5">AI 시선 추적 기반 수업 집중도 평균</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-3 justify-between sm:justify-end pl-12 sm:pl-0 w-full sm:w-auto">
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input :checked="modelValue.use_engagement_criteria"
                                @change="updateField('use_engagement_criteria', ($event.target as HTMLInputElement).checked)"
                                class="sr-only peer" type="checkbox" />
                            <div
                                class="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
                            </div>
                        </label>
                        <div class="h-8 w-px bg-slate-300 hidden sm:block mx-1"></div>
                        <div class="flex items-center gap-2"
                            :class="modelValue.use_engagement_criteria ? '' : 'opacity-50 grayscale pointer-events-none'">
                            <div class="relative w-24">
                                <input :value="modelValue.completion_engagement_score"
                                    @input="updateField('completion_engagement_score', Number(($event.target as HTMLInputElement).value))"
                                    class="w-full rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors text-slate-800 text-right pr-6 bg-white shadow-sm font-bold border-slate-300 border"
                                    type="number" :disabled="!modelValue.use_engagement_criteria" />
                                <span class="absolute right-2 top-2 text-slate-400 font-bold text-xs">%</span>
                            </div>
                            <span class="text-xs font-bold text-slate-700 whitespace-nowrap min-w-[2rem]">이상</span>
                        </div>
                    </div>
                </div>

                <!-- Exit Quiz -->
                <div
                    class="bg-slate-50 rounded-lg border border-slate-200 p-3 flex flex-col sm:flex-row sm:items-center gap-3 transition-shadow hover:shadow-md hover:border-indigo-200 group">
                    <div class="flex items-center gap-3 flex-1">
                        <div
                            class="size-10 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center shrink-0 shadow-sm">
                            <span class="material-symbols-outlined text-xl">quiz</span>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h4 class="font-bold text-slate-900 text-sm">퇴실 퀴즈 평균 점수 (Exit Quiz)</h4>
                                <span
                                    class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-slate-200 text-slate-600 border border-slate-300">선택</span>
                            </div>
                            <p class="text-[11px] text-slate-500 mt-0.5">매 세션 종료 후 진행되는 퀴즈 점수</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-3 justify-between sm:justify-end pl-12 sm:pl-0 w-full sm:w-auto">
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input :checked="modelValue.use_quiz_criteria"
                                @change="updateField('use_quiz_criteria', ($event.target as HTMLInputElement).checked)"
                                class="sr-only peer" type="checkbox" />
                            <div
                                class="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
                            </div>
                        </label>
                        <div class="h-8 w-px bg-slate-300 hidden sm:block mx-1"></div>
                        <div class="flex items-center gap-2"
                            :class="modelValue.use_quiz_criteria ? '' : 'opacity-50 grayscale pointer-events-none'">
                            <div class="relative w-24">
                                <input :value="modelValue.completion_quiz_score"
                                    @input="updateField('completion_quiz_score', Number(($event.target as HTMLInputElement).value))"
                                    class="w-full rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors text-slate-800 text-right pr-6 bg-white shadow-sm font-bold border-slate-300 border"
                                    type="number" :disabled="!modelValue.use_quiz_criteria" />
                                <span class="absolute right-2 top-2 text-slate-400 font-bold text-xs">점</span>
                            </div>
                            <span class="text-xs font-bold text-slate-700 whitespace-nowrap min-w-[2rem]">이상</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
const props = defineProps<{
    modelValue: any;
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', value: any): void;
}>();

const updateField = (field: string, value: any) => {
    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};
</script>

<template>
    <div class="space-y-4">
        <!-- AI 학습 분석 설정 -->
        <div
            class="bg-white rounded-xl p-4 border border-slate-200 shadow-sm relative overflow-hidden transition-all hover:border-indigo-200">
            <div class="flex items-center gap-2 mb-3">
                <span class="bg-slate-700 text-white text-[10px] font-bold px-1.5 py-0.5 rounded">STEP 3</span>
                <h3 class="font-bold text-base text-slate-800">AI 학습 분석 설정</h3>
            </div>
            <div class="space-y-2">
                <!-- Engagement Check -->
                <div
                    class="flex items-center justify-between p-3 bg-slate-50 border border-slate-200 rounded-lg transition-colors hover:bg-white hover:border-indigo-200 hover:shadow-sm group">
                    <div class="flex items-start gap-3">
                        <div
                            class="p-2 bg-blue-100 text-blue-600 rounded-lg shrink-0 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                            <span class="material-symbols-outlined text-[18px]">visibility</span>
                        </div>
                        <div>
                            <h4 class="font-bold text-slate-900 text-xs">집중도 체크 (Engagement Check)</h4>
                            <p class="text-[11px] text-slate-500 mt-0.5 leading-snug">학습자의 시선 추적 및 화면 이탈 여부를 분석하여 실시간 학습
                                집중도를 측정합니다.</p>
                        </div>
                    </div>
                    <label class="relative inline-flex items-center cursor-pointer ml-3">
                        <input :checked="modelValue.isEngageCheck"
                            @change="updateField('isEngageCheck', ($event.target as HTMLInputElement).checked)"
                            class="sr-only peer" type="checkbox" />
                        <div
                            class="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
                        </div>
                    </label>
                </div>

                <!-- Sentiment Analysis -->
                <div
                    class="flex items-center justify-between p-3 bg-slate-50 border border-slate-200 rounded-lg transition-colors hover:bg-white hover:border-indigo-200 hover:shadow-sm group">
                    <div class="flex items-start gap-3">
                        <div
                            class="p-2 bg-purple-100 text-purple-600 rounded-lg shrink-0 group-hover:bg-purple-600 group-hover:text-white transition-colors">
                            <span class="material-symbols-outlined text-[18px]">sentiment_satisfied</span>
                        </div>
                        <div>
                            <h4 class="font-bold text-slate-900 text-xs">감정 분석 (Sentiment Analysis)</h4>
                            <p class="text-[11px] text-slate-500 mt-0.5 leading-snug">토론 및 Q&amp;A 게시글의 텍스트 톤앤매너를 분석하여
                                학습자의 정서 상태를 파악합니다.</p>
                        </div>
                    </div>
                    <label class="relative inline-flex items-center cursor-pointer ml-3">
                        <input :checked="modelValue.isSentimentAnalysis"
                            @change="updateField('isSentimentAnalysis', ($event.target as HTMLInputElement).checked)"
                            class="sr-only peer" type="checkbox" />
                        <div
                            class="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
                        </div>
                    </label>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
const props = defineProps<{
    modelValue: any; // Using any for extended properties
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

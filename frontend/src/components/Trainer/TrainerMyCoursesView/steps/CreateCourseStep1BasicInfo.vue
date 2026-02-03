<template>
    <div class="space-y-4">
        <!-- Title -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">과정명</label>
            <input :value="modelValue.title" @input="updateField('title', ($event.target as HTMLInputElement).value)"
                type="text"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                placeholder="예: 실전 마케팅 전략 마스터" required />
        </div>

        <!-- Description -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">한줄 소개</label>
            <textarea :value="modelValue.description"
                @input="updateField('description', ($event.target as HTMLInputElement).value)"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400 resize-none"
                rows="3" placeholder="과정에 대한 간략한 설명을 입력하세요."></textarea>
        </div>

        <div class="grid grid-cols-2 gap-4">
            <!-- Category -->
            <div>
                <label class="block text-sm font-bold text-slate-700 mb-1.5">카테고리</label>
                <select :value="modelValue.category"
                    @change="updateField('category', ($event.target as HTMLSelectElement).value)"
                    class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors bg-white">
                    <option value="" disabled>선택하세요</option>
                    <option value="BUSINESS">비즈니스</option>
                    <option value="DEV">개발/프로그래밍</option>
                    <option value="DATA">데이터 사이언스</option>
                    <option value="MARKETING">마케팅</option>
                    <option value="DESIGN">디자인</option>
                    <option value="FINANCE">재무/회계</option>
                    <option value="LANGUAGE">외국어</option>
                    <option value="ETC">기타</option>
                </select>
            </div>
            <!-- Max Learner -->
            <div>
                <label class="block text-sm font-bold text-slate-700 mb-1.5">최대 수강 인원</label>
                <div class="relative">
                    <input :value="modelValue.maxLearner"
                        @input="updateField('maxLearner', Number(($event.target as HTMLInputElement).value))"
                        type="number"
                        class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400 pr-8"
                        placeholder="예: 30" min="0" />
                    <span class="absolute right-3 top-2 text-slate-400 text-sm">명</span>
                </div>
            </div>
        </div>
        <!-- Sub Category -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">서브 카테고리</label>
            <input :value="modelValue.subCategory"
                @input="updateField('subCategory', ($event.target as HTMLInputElement).value)" type="text"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                placeholder="예: Backend " />
        </div>
    </div>
</template>

<script setup lang="ts">
import type { CreateCourse } from '@/types';

const props = defineProps<{
    modelValue: Partial<CreateCourse>;
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', value: Partial<CreateCourse>): void;
}>();

const updateField = (field: keyof CreateCourse, value: any) => {
    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};
</script>

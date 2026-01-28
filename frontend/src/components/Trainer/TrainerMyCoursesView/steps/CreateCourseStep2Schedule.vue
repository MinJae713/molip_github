<template>
    <div class="space-y-4">
        <!-- Period -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">운영 기간</label>
            <input :value="modelValue.period" @input="updateField('period', ($event.target as HTMLInputElement).value)"
                type="text"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                placeholder="예: 2024.01.01 ~ 2024.03.31 (12주)" />
            <p class="text-xs text-slate-500 mt-1">과정이 진행되는 전체 기간을 입력해주세요.</p>
        </div>

        <!-- Duration -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">총 강의 시간 (예상)</label>
            <input :value="modelValue.duration"
                @input="updateField('duration', ($event.target as HTMLInputElement).value)" type="text"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                placeholder="예: 40시간" />
        </div>

        <!-- Total Lessons -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">총 차시 수</label>
            <input :value="modelValue.total_lessons"
                @input="updateField('total_lessons', Number(($event.target as HTMLInputElement).value))" type="number"
                class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                placeholder="예: 20" />
        </div>
    </div>
</template>

<script setup lang="ts">
import type { Course } from '@/types';

const props = defineProps<{
    modelValue: Partial<Course>;
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', value: Partial<Course>): void;
}>();

const updateField = (field: keyof Course, value: any) => {
    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};
</script>

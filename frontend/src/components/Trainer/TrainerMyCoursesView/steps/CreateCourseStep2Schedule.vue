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

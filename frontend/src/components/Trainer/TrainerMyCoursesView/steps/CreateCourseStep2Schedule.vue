<template>
    <div class="space-y-4">
        <!-- Period -->
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5">운영 기간 (주 단위)</label>
            <div class="relative w-32">
                <input :value="modelValue.period"
                    @input="updateField('period', Number(($event.target as HTMLInputElement).value))" type="number"
                    class="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400 pr-8"
                    placeholder="예: 8" min="0" />
                <span class="absolute right-3 top-2 text-slate-400 text-sm">주</span>
            </div>
            <p class="text-xs text-slate-500 mt-1">과정이 진행되는 전체 기간(주 수)을 입력해주세요.</p>
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

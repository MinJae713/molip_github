<template>
    <div class="px-6 py-6">
        <div class="flex items-center justify-between mb-6 px-4">
            <template v-for="(step, index) in steps" :key="index">
                <!-- Step Item -->
                <div class="flex flex-col items-center relative z-10 w-24" :class="{
                    'step-completed': currentStep > index + 1,
                    'step-active': currentStep === index + 1,
                    'step-pending': currentStep < index + 1
                }">
                    <div class="mb-1.5 w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold border-2 transition-colors"
                        :class="{
                            'bg-indigo-600 border-indigo-600 text-white shadow-md shadow-indigo-200': currentStep === index + 1,
                            'bg-indigo-600 border-indigo-600 text-white': currentStep > index + 1,
                            'bg-white border-slate-300 text-slate-400': currentStep < index + 1
                        }">
                        <span v-if="currentStep > index + 1"
                            class="material-symbols-outlined text-base font-bold">check</span>
                        <span v-else>{{ index + 1 }}</span>
                    </div>
                    <span class="text-[11px] font-bold"
                        :class="currentStep >= index + 1 ? 'text-indigo-700' : 'text-slate-400'">
                        {{ step }}
                    </span>
                </div>

                <!-- Connector Line (except after last item) -->
                <div v-if="index < steps.length - 1" class="flex-1 h-0.5 mx-[-1rem] mb-5 relative"
                    :class="currentStep > index + 1 ? 'bg-indigo-600' : 'bg-slate-200'"></div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    currentStep: number;
    steps: string[];
}>();
</script>

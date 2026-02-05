<script setup lang="ts">
interface Props {
  title: string;
  value: string | number;
  icon: string;
  variant?: 'indigo' | 'blue' | 'amber' | 'emerald';
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'indigo'
});

const variantClasses = {
  indigo: 'bg-indigo-50 text-indigo-600',
  blue: 'bg-blue-50 text-blue-600',
  amber: 'bg-amber-50 text-amber-600',
  emerald: 'bg-emerald-50 text-emerald-600',
} as const;
</script>

<template>
  <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col justify-between h-full min-h-40">
    
    <div class="flex items-start justify-between">
      <div :class="['size-12 rounded-xl flex items-center justify-center', variantClasses[variant]]">
        <span class="material-symbols-outlined text-[24px]">{{ icon }}</span>
      </div>
      
      <slot name="badge"></slot>
    </div>

    <div class="mt-4">
      <p class="text-sm font-medium text-slate-500 mb-1">{{ title }}</p>
      <div class="flex flex-col gap-1">
        <h3 class="text-3xl font-bold text-slate-900">{{ value }}</h3>
        
        <slot name="footer"></slot>
      </div>
    </div>
    
  </div>
</template>
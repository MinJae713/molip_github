<script setup lang="ts">
interface Props {
  active: boolean;
  activeIcon: string;
  inactiveIcon: string;
  label?: string;
}

defineProps<Props>();
defineEmits(['toggle']);
</script>

<template>
  <button 
    @click="$emit('toggle')"
    :class="[
      'flex flex-col items-center justify-center p-3 rounded-xl transition-all duration-200',
      active ? 'bg-white text-slate-900 shadow-sm' : 'bg-slate-800 text-white hover:bg-slate-700'
    ]"
  >
    <span class="material-symbols-outlined text-[28px]">
      {{ active ? activeIcon : inactiveIcon }}
    </span>
    <span v-if="label" class="text-[11px] mt-1 font-medium">{{ label }}</span>
  </button>
</template>
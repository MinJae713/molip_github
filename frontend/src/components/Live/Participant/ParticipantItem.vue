<script setup lang="ts">
import type { Participant } from '@/types/live';

defineProps<{ participant: Participant }>();
</script>

<template>
  <div class="flex items-center justify-between p-3 rounded-2xl hover:bg-slate-50 transition-colors group">
    <div class="flex items-center gap-3">
      <div class="relative">
        <img :src="participant.avatar" class="size-10 rounded-full object-cover" />
        <div 
          :class="[
            'absolute bottom-0 right-0 size-3 border-2 border-white rounded-full',
            participant.status === 'focus' ? 'bg-emerald-500' : 'bg-amber-500'
          ]"
        ></div>
      </div>
      
      <div class="flex flex-col">
        <span class="text-sm font-bold text-slate-800">{{ participant.name }}</span>
        <span class="text-[11px] font-medium text-slate-400">집중도 {{ participant.concentrationScore }}%</span>
      </div>
    </div>

    <div class="flex items-center gap-2">
      <span 
        class="material-symbols-outlined text-[18px]"
        :class="participant.isMicOn ? 'text-slate-400' : 'text-red-400'"
      >
        {{ participant.isMicOn ? 'mic' : 'mic_off' }}
      </span>
      <span 
        class="material-symbols-outlined text-[18px]"
        :class="participant.isCameraOn ? 'text-slate-400' : 'text-red-400'"
      >
        {{ participant.isCameraOn ? 'videocam' : 'videocam_off' }}
      </span>
    </div>
  </div>
</template>
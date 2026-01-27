<script setup lang="ts">
import { useLiveStore } from '@/stores/live';
import MediaButton from './MediaButton.vue';

const liveStore = useLiveStore();
</script>

<template>
  <div class="fixed bottom-6 left-1/2 -translate-x-1/2 w-[90%] max-w-4xl z-50">
    <div class="bg-slate-900/90 backdrop-blur-md p-4 rounded-2xl shadow-2xl border border-slate-700/50 flex items-center justify-between">
      
      <div class="flex items-center gap-3">
        <MediaButton 
          :active="liveStore.isMicOn" 
          active-icon="mic" 
          inactive-icon="mic_off" 
          label="마이크"
          @toggle="liveStore.toggleMic"
        />
        <MediaButton 
          :active="liveStore.isCamOn" 
          active-icon="videocam" 
          inactive-icon="videocam_off" 
          label="카메라"
          @toggle="liveStore.toggleCamera"
        />
        </div>

      </div>
  </div>
</template>
<script setup lang="ts">
import { useLiveStore } from '@/stores/live';
import LocalVideoView from '../Monitor/LocalVideoView.vue';
import MediaButton from '../Monitor/MediaButton.vue';

const liveStore = useLiveStore();

// 부모 컴포넌트(InstructorConsole)로 종료 이벤트를 보낼 필요가 없어졌으므로 
// emit 정의는 삭제하거나 유지해도 무방합니다.
</script>

<template>
  <div class="flex flex-col bg-slate-950 rounded-[2.5rem] overflow-hidden shadow-2xl border-4 border-white shrink-0">
    
    <div class="relative w-full aspect-video bg-black flex items-center justify-center overflow-hidden">
      
      <template v-if="liveStore.isScreenSharing">
        <video 
          :srcObject="liveStore.screenStream" 
          autoplay 
          class="w-full h-full object-contain"
        ></video>
      </template>
      
      <template v-else>
        <LocalVideoView 
          v-if="liveStore.isCamOn"
          :stream="liveStore.myStream" 
          :is-muted="true" 
          class="w-full h-full object-cover mirror"
        />
        <div v-else class="flex flex-col items-center text-slate-700">
          <span class="material-symbols-outlined text-6xl mb-3">videocam_off</span>
          <p class="font-bold text-slate-500">카메라가 꺼져 있습니다</p>
        </div>
      </template>

      <Transition name="fade">
        <div 
          v-if="liveStore.isScreenSharing && liveStore.isCamOn"
          class="absolute top-6 right-6 z-20 w-56 aspect-video bg-slate-900 rounded-3xl border-2 border-white/20 shadow-2xl overflow-hidden"
        >
          <LocalVideoView 
            :stream="liveStore.myStream" 
            :is-muted="true" 
            class="w-full h-full object-cover mirror"
          />
          <div class="absolute bottom-2 left-2 flex items-center gap-1.5 bg-black/40 px-2 py-1 rounded-lg backdrop-blur-md">
            <span class="size-1.5 bg-indigo-500 rounded-full animate-pulse"></span>
            <span class="text-[9px] text-white font-black tracking-tighter uppercase">Instructor (Live)</span>
          </div>
        </div>
      </Transition>
    </div>

    <div class="bg-slate-900 px-8 py-5 border-t border-slate-800 flex items-center justify-between">
      
      <div class="flex items-center gap-4">
        <MediaButton 
          :active="liveStore.isMicOn" 
          active-icon="mic" 
          inactive-icon="mic_off"
          @toggle="liveStore.toggleMic"
        />
        <MediaButton 
          :active="liveStore.isCamOn" 
          active-icon="videocam" 
          inactive-icon="videocam_off"
          @toggle="liveStore.toggleCamera"
        />
        
        <div class="w-px h-8 bg-slate-800 mx-2"></div>
        
        <button 
          @click="liveStore.toggleScreenShare"
          :class="[
            'flex items-center gap-2 px-6 py-2.5 rounded-full font-bold text-sm transition-all',
            liveStore.isScreenSharing ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'bg-white/10 text-white hover:bg-white/20'
          ]"
        >
          <span class="material-symbols-outlined text-[20px]">
            {{ liveStore.isScreenSharing ? 'stop_screen_share' : 'present_to_all' }}
          </span>
          {{ liveStore.isScreenSharing ? '공유 중지' : '화면 공유' }}
        </button>
      </div>

      <div class="flex items-center gap-3 px-6 py-2.5 bg-black/30 rounded-full border border-white/5">
        <span class="text-indigo-400 material-symbols-outlined text-[20px]">psychology</span>
        <span class="text-slate-300 text-sm font-medium italic">AI 분석 연동 중...</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 강사 본인 화면은 거울 모드 적용 */
.mirror { 
  transform: scaleX(-1); 
}

/* PIP 나타날 때 부드러운 페이드 효과 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

video {
  background-color: #000;
}
</style>
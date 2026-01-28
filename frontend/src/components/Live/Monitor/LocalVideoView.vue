<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

interface Props {
  stream: MediaStream | null;
  isMuted?: boolean;
}

const props = defineProps<Props>();
const videoRef = ref<HTMLVideoElement | null>(null);

// 스트림이 바뀌면 비디오 태그에 연결
watch(() => props.stream, (newStream) => {
  if (videoRef.value && newStream) {
    videoRef.value.srcObject = newStream;
  }
}, { immediate: true });

onMounted(() => {
  if (videoRef.value && props.stream) {
    videoRef.value.srcObject = props.stream;
  }
});
</script>

<template>
  <div class="relative w-full h-full bg-slate-950 rounded-2xl overflow-hidden border border-slate-800 shadow-2xl">
    <video
      ref="videoRef"
      autoplay
      playsinline
      :muted="isMuted"
      class="w-full h-full object-cover scale-x-[-1]"
    ></video>
    
    <div class="absolute bottom-4 left-4 flex items-center gap-2">
      <div class="bg-black/50 backdrop-blur-md px-3 py-1 rounded-lg text-white text-xs font-medium">
        나 (강사)
      </div>
    </div>
  </div>
</template>
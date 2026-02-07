<!-- src/components/Learner/LearnerReviewView/ReviewVideoPlayer.vue -->
<template>
    <div class="bg-black rounded-2xl overflow-hidden shadow-card group relative aspect-video flex-none">
        <!-- 비디오 영역 -->
        <video ref="videoRef" class="w-full h-full object-contain cursor-pointer" :src="testVideoUrl" :poster="djMaxImg"
            @click="togglePlay" @loadedmetadata="onLoadedMetadata" @timeupdate="onTimeUpdate"
            @ended="isPlaying = false"></video>
        <!-- 중앙 재생 버튼 (일시정지 상태에서만 표시) -->
        <div v-if="!isPlaying"
            class="absolute inset-0 flex items-center justify-center bg-black/20 pointer-events-none transition-all duration-300">
            <button @click.stop="togglePlay"
                class="pointer-events-auto size-20 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center text-white hover:scale-110 hover:bg-white/30 transition-all duration-300 group-hover:shadow-glow border border-white/30">
                <span class="material-symbols-outlined text-5xl fill-1 ml-1">play_arrow</span>
            </button>
        </div>
        <!-- 하단 컨트롤 바 (호버) -->
        <div
            class="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/90 via-black/40 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
            <!-- 진행 막대 -->
            <div class="w-full bg-white/20 h-1.5 rounded-full mb-4 cursor-pointer relative group/timeline"
                @click="seek">
                <div class="bg-indigo-500 h-full rounded-full relative transition-all duration-100 ease-linear"
                    :style="{ width: progressPercent + '%' }">
                    <!-- 핸들 -->
                    <div
                        class="absolute right-0 top-1/2 -translate-y-1/2 size-3 bg-white rounded-full shadow-lg scale-0 group-hover/timeline:scale-100 transition-transform">
                    </div>
                </div>
            </div>
            <!-- 컨트롤 버튼 그룹 -->
            <div class="flex items-center justify-between text-white">
                <div class="flex items-center gap-4">
                    <!-- 재생버튼 -->
                    <button @click="togglePlay" class="hover:text-indigo-400 transition-colors">
                        <span class="material-symbols-outlined text-3xl">
                            {{ isPlaying ? 'pause' : 'play_arrow' }}
                        </span>
                    </button>
                    <!-- 볼륨 조절 그룹 -->
                    <div class="flex items-center gap-2">
                        <button @click="toggleMute" class="transition-colors">
                            <span class="material-symbols-outlined text-[22px]">
                                {{ isMuted || volume === 0 ? 'volume_off' : volume < 0.5 ? 'volume_down' : 'volume_up'
                                    }} </span>
                        </button>
                        <input type="range" min="0" max="1" step="0.05" :value="isMuted ? 0 : volume"
                            @input="handleVolumeChange"
                            class="w-20 h-1 bg-white/30 rounded-full appearance-none cursor-pointer accent-indigo-500" />
                    </div>
                    <!-- 시간 정보 -->
                    <div class="text-[13px] font-medium font-mono ml-2">
                        <span class="text-indigo-400">{{ formatTime(currentTime) }}</span>
                        <span class="mx-1 text-white/50">/</span>
                        <span>{{ formatTime(duration) }}</span>
                    </div>
                </div>
                <!-- 오른쪽 설정툴 -->
                <div class="flex items-center gap-4">
                    <button @click="toggleFullscreen"
                        class="hover:text-indigo-400 transition-colors opacity-70 hover:opacity-100">
                        <span class="material-symbols-outlined text-[24px]">fullscreen</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue';
import djMaxImg from '@/assets/thumbnail.png';
// 테스트 영상 URL (나중에 S3 URL이 들어올 자리)
const testVideoUrl = "https://d285ryfcbcw5yb.cloudfront.net/ssafy_play_video_1.mp4";
// 비디오 상태 관련 변수
const videoRef = ref<HTMLVideoElement | null>(null);
const isPlaying = ref(false);
const duration = ref(0);
const currentTime = ref(0);
const volume = ref(1);
const isMuted = ref(false);
// 시간 데이터 바인딩
const onLoadedMetadata = () => {
    if (videoRef.value) duration.value = videoRef.value.duration;
};
const onTimeUpdate = () => {
    if (videoRef.value) currentTime.value = videoRef.value.currentTime;
};
// 비디오 제어 로직
const togglePlay = () => {
    if (!videoRef.value) return;
    if (videoRef.value.paused) {
        videoRef.value.play();
        isPlaying.value = true;
    } else {
        videoRef.value.pause();
        isPlaying.value = false;
    }
};
// 진행시간 포맷팅
const progressPercent = computed(() => {
    if (duration.value === 0) return 0;
    return (currentTime.value / duration.value) * 100;
});
const formatTime = (seconds: number) => {
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60);
    return `${min}:${sec.toString().padStart(2, '0')}`;
};
// 클릭 이동
const seek = (event: MouseEvent) => {
    if (!videoRef.value) return;
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const clickX = event.clientX - rect.left;
    const progressRatio = clickX / rect.width;
    videoRef.value.currentTime = progressRatio * duration.value;
};
// 볼륨 및 음소거 제어
const toggleMute = () => {
    if (!videoRef.value) return;
    isMuted.value = !isMuted.value;
    videoRef.value.muted = isMuted.value;
};
const handleVolumeChange = (event: Event) => {
    if (!videoRef.value) return;
    const val = (event.target as HTMLInputElement).value;
    volume.value = parseFloat(val);
    videoRef.value.volume = volume.value;
    if (volume.value > 0) {
        isMuted.value = false;
        videoRef.value.muted = false;
    }
};
// 전체화면
const toggleFullscreen = () => {
    if (!videoRef.value) return;
    if (!document.fullscreenElement) {
        // 비디오를 감싸는 부모 div를 전체화면으로 만들어야 컨트롤 바가 보임
        videoRef.value.parentElement?.requestFullscreen();
    } else {
        document.exitFullscreen();
    }
};

// 외부 제어용 함수 노출
const seekTo = (seconds: number) => {
    if (!videoRef.value) return;
    videoRef.value.currentTime = seconds;
    if (!isPlaying.value) {
         // 이동 후 자동 재생 여부는 기획에 따라 결정 (여기서는 이동만 함)
         // videoRef.value.play();
         // isPlaying.value = true;
    }
};

defineExpose({
    seekTo
});
</script>
<style scoped>
/* 구글 아이콘 버튼 기본 설정 */
.material-symbols-outlined {
    font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
    display: block;
}

/* 슬라이더 커스텀 스타일 (전체 테마 색상 맞춤) */
input[type='range']::-webkit-slider-thumb {
    appearance: none;
    width: 12px;
    height: 12px;
    background: white;
    border-radius: 50%;
    cursor: pointer;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}
</style>
<template>
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
        <div class="bg-white rounded-2xl w-full max-w-2xl p-8 shadow-2xl animate-fade-in-up">
            <h2 class="text-2xl font-bold mb-6 text-gray-800 text-center">강의실 입장 전 설정</h2>

            <div class="flex flex-col md:flex-row gap-8">
                <!-- 미리보기 화면 -->
                <div class="flex-1 relative aspect-video bg-black rounded-xl overflow-hidden shadow-inner group">
                    <video ref="videoRef" autoplay muted playsinline
                        class="w-full h-full object-cover transform scale-x-[-1]"></video>

                    <div
                        class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                        <div
                            class="w-10 h-10 rounded-full bg-black/50 flex items-center justify-center backdrop-blur text-white">
                            <span v-if="localStream?.getVideoTracks()[0]?.enabled">📷</span>
                            <span v-else>🚫</span>
                        </div>
                        <div
                            class="w-10 h-10 rounded-full bg-black/50 flex items-center justify-center backdrop-blur text-white">
                            <span v-if="localStream?.getAudioTracks()[0]?.enabled">🎤</span>
                            <span v-else>🔇</span>
                        </div>
                    </div>

                    <div v-if="isVideoLoading"
                        class="absolute inset-0 flex items-center justify-center bg-gray-900 text-white">
                        <span class="animate-pulse">카메라 연결 중...</span>
                    </div>

                    <div v-if="errorMessage"
                        class="absolute inset-0 flex flex-col items-center justify-center bg-gray-900/90 text-red-400 p-6 text-center z-10 transition-opacity">
                        <span class="material-symbols-outlined text-4xl mb-2">warning</span>
                        <p class="font-bold mb-1">카메라 연결 실패</p>
                        <p class="text-sm text-gray-300 break-keep">{{ errorMessage }}</p>
                        <button @click="retryPreview"
                            class="mt-4 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-200 text-sm rounded-lg transition-colors border border-red-500/30">
                            다시 시도
                        </button>
                    </div>
                </div>

                <!-- 설정 컨트롤 -->
                <div class="flex-1 flex flex-col gap-5 justify-center">

                    <!-- 카메라 선택 -->
                    <div class="space-y-2">
                        <label class="text-sm font-semibold text-gray-600 block">카메라</label>
                        <div class="relative">
                            <select v-model="selectedVideo" @change="handleVideoChange"
                                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none appearance-none cursor-pointer transition-all">
                                <option v-for="device in videoStore.videoDevices" :key="device.deviceId"
                                    :value="device.deviceId">
                                    {{ device.label || `Camera ${device.deviceId.slice(0, 5)}...` }}
                                </option>
                            </select>
                        </div>
                    </div>

                    <!-- 마이크 선택 -->
                    <div class="space-y-2">
                        <label class="text-sm font-semibold text-gray-600 block">마이크</label>
                        <div class="relative">
                            <select v-model="selectedAudio" @change="handleAudioChange"
                                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none appearance-none cursor-pointer transition-all">
                                <option v-for="device in videoStore.audioDevices" :key="device.deviceId"
                                    :value="device.deviceId">
                                    {{ device.label || `Microphone ${device.deviceId.slice(0, 5)}...` }}
                                </option>
                            </select>
                        </div>
                    </div>

                    <!-- 입장 버튼 -->
                    <button @click="emit('join')"
                        class="mt-4 w-full py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl shadow-lg transform transition hover:scale-[1.02] active:scale-[0.98]">
                        입장하기
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useVideoRoomStore } from '@/stores/videoRoom';

const emit = defineEmits(['join']);
const videoStore = useVideoRoomStore();
const videoRef = ref<HTMLVideoElement | null>(null);
const isVideoLoading = ref(true);
const errorMessage = ref("");

const selectedVideo = ref('');
const selectedAudio = ref('');

const localStream = computed(() => videoStore.localStream);

// 초기화
onMounted(async () => {
    await videoStore.getDevices();
    selectedVideo.value = videoStore.selectedVideoDeviceId;
    selectedAudio.value = videoStore.selectedAudioDeviceId;

    await startPreview();
});

async function startPreview() {
    isVideoLoading.value = true;
    errorMessage.value = "";
    try {
        const stream = await videoStore.startLocalPreview();
        if (videoRef.value && stream) {
            videoRef.value.srcObject = stream;
        } else {
            // 스트림이 없거나 비디오 ref가 없을 때 경고 (개발용)
            console.warn("Stream ready but videoRef missing or stream null");
        }
    } catch (e: any) {
        console.error("Preview failed", e);
        // 사용자에게 보여줄 메시지
        let msg = "장치에 접근할 수 없습니다.";
        if (e.name === "NotAllowedError" || e.name === "PermissionDeniedError") {
            msg = "카메라/마이크 권한이 차단되었습니다. 브라우저 설정에서 권한을 허용해주세요.";
        } else if (e.name === "NotFoundError" || e.name === "DevicesNotFoundError") {
            msg = "카메라 또는 마이크를 찾을 수 없습니다.";
        } else if (e.name === "NotReadableError" || e.name === "TrackStartError") {
            msg = "장치가 이미 다른 앱에서 사용 중일 수 있습니다.";
        }
        errorMessage.value = msg;
    } finally {
        isVideoLoading.value = false;
    }
}

async function retryPreview() {
    await videoStore.getDevices();
    await startPreview();
}

async function handleVideoChange() {
    await videoStore.setVideoDevice(selectedVideo.value);
    if (videoRef.value && videoStore.localStream) {
        videoRef.value.srcObject = videoStore.localStream;
    }
}

async function handleAudioChange() {
    await videoStore.setAudioDevice(selectedAudio.value);
    // Audio change doesn't necessarily need stream re-attach if track was replaced, 
    // but startLocalPreview replaces the whole stream, so re-attach is safer.
    if (videoRef.value && videoStore.localStream) {
        videoRef.value.srcObject = videoStore.localStream;
    }
}

// 스트림 변경 감지 (Store에서 변경되었을 경우)
watch(() => videoStore.localStream, (newStream) => {
    if (videoRef.value && newStream) {
        videoRef.value.srcObject = newStream;
    }
});

</script>

<style scoped>
.animate-fade-in-up {
    animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>

<template>
    <!-- [NEW] Full Screen Calibration Overlay -->
    <div v-if="isCalibrationMode" class="fixed inset-0 z-[60] bg-black flex flex-col items-center justify-center">
        <!-- Center Target (Blue) - Fixed at screen center -->
        <div
            class="absolute top-1/2 left-1/2 w-8 h-8 bg-blue-500 rounded-full -translate-x-1/2 -translate-y-1/2 shadow-[0_0_30px_rgba(59,130,246,1)] border-4 border-white z-20">
            <div
                class="absolute top-full mt-4 left-1/2 -translate-x-1/2 text-blue-300 font-bold text-lg whitespace-nowrap animate-pulse">
                이 곳을 응시하세요
            </div>
        </div>

        <!-- Current Gaze (Red) - Visual Feedback -->
        <div class="absolute w-6 h-6 bg-red-500 rounded-full shadow-[0_0_20px_rgba(239,68,68,1)] transition-all duration-75 ease-out -translate-x-1/2 -translate-y-1/2 z-30 opacity-80"
            :style="{
                left: `${currentGaze.x * 100}%`,
                top: `${currentGaze.y * 100}%`
            }">
        </div>

        <!-- Instructions & Controls -->
        <div
            class="absolute bottom-20 flex flex-col items-center gap-6 z-40 bg-gray-900/80 p-8 rounded-2xl backdrop-blur-md border border-gray-700 transition-all duration-300">
            <div class="text-white text-center space-y-2">
                <h3 class="text-2xl font-bold">
                    {{ isCalibrating ? '화면 중앙을 응시하세요' : '시선 중앙 보정' }}
                </h3>
                <p class="text-gray-300">
                    {{ isCalibrating
                        ? '3초간 파란 점을 계속 바라봐주세요.'
                        : '화면 중앙의 파란 점을 응시할 준비가 되면 시작 버튼을 누르세요.'
                    }}
                </p>
            </div>

            <!-- Action Area -->
            <div v-if="!isCalibrating" class="flex gap-4">
                <button @click="startCalibration"
                    class="px-8 py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-lg rounded-xl shadow-lg transition-transform active:scale-95 flex items-center gap-2">
                    <span>▶ 시작 (Start)</span>
                </button>
                <button @click="isCalibrationMode = false"
                    class="px-8 py-4 bg-gray-700 hover:bg-gray-600 text-white font-bold text-lg rounded-xl shadow-lg transition-colors">
                    취소 (Cancel)
                </button>
            </div>

            <!-- Progress Bar -->
            <div v-else class="w-64 h-4 bg-gray-700 rounded-full overflow-hidden relative">
                <div class="absolute top-0 left-0 h-full bg-blue-500 transition-all duration-100 ease-linear"
                    :style="{ width: `${calibrationProgress}%` }">
                </div>
            </div>
        </div>
    </div>

    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
        <div class="bg-white rounded-2xl w-full max-w-2xl p-8 shadow-2xl animate-fade-in-up">
            <h2 class="text-2xl font-bold mb-6 text-gray-800 text-center">강의실 입장 및 시선 보정</h2>

            <div class="flex flex-col md:flex-row gap-8">
                <!-- 미리보기 화면 -->
                <div class="flex-1 relative aspect-video bg-black rounded-xl overflow-hidden shadow-inner group">
                    <video ref="videoRef" autoplay muted playsinline
                        class="w-full h-full object-cover transform scale-x-[-1]"></video>

                    <!-- [REMOVED] Calibration Overlay inside preview - Moved to Full Screen -->


                    <div
                        class="absolute bottom-4 left-4 flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity z-30">
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
                        class="absolute inset-0 flex items-center justify-center bg-gray-900 text-white z-40">
                        <span class="animate-pulse">카메라 연결 중...</span>
                    </div>

                    <div v-if="errorMessage"
                        class="absolute inset-0 flex flex-col items-center justify-center bg-gray-900/90 text-red-400 p-6 text-center z-50 transition-opacity">
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

                    <!-- [NEW] Calibration Button triggers Full Screen Mode -->
                    <button @click="isCalibrationMode = true"
                        class="w-full py-3 bg-slate-700 hover:bg-slate-800 text-white font-bold rounded-xl shadow-md transition-colors flex items-center justify-center gap-2">
                        <span>📏 전체 화면으로 보정하기</span>
                    </button>

                    <!-- 입장 버튼 -->
                    <button @click="emit('join')"
                        class="mt-2 w-full py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl shadow-lg transform transition hover:scale-[1.02] active:scale-[0.98]">
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

const emit = defineEmits(['join', 'calibrate']);
const props = defineProps<{
    currentGaze: { x: number, y: number },
    rawGaze?: { x: number, y: number } // [NEW] For averaging
}>();

const videoStore = useVideoRoomStore();
const videoRef = ref<HTMLVideoElement | null>(null);
const isVideoLoading = ref(true);
const errorMessage = ref("");
const isCalibrationMode = ref(false);

// Calibration State
const isCalibrating = ref(false);
const calibrationProgress = ref(0);
const rawGazeBuffer = ref<{ x: number, y: number }[]>([]);

const selectedVideo = ref('');
const selectedAudio = ref('');

const localStream = computed(() => videoStore.localStream);

// Watch for rawGaze during calibration
watch(() => props.rawGaze, (newVal) => {
    if (isCalibrating.value && newVal) {
        rawGazeBuffer.value.push(newVal);
    }
});

const startCalibration = () => {
    if (isCalibrating.value) return;

    isCalibrating.value = true;
    calibrationProgress.value = 0;
    rawGazeBuffer.value = [];

    // 3 seconds timer
    const duration = 10000;
    const interval = 50;
    const steps = duration / interval;
    let currentStep = 0;

    const timer = setInterval(() => {
        currentStep++;
        calibrationProgress.value = Math.min((currentStep / steps) * 100, 100);

        if (currentStep >= steps) {
            clearInterval(timer);
            finishCalibration();
        }
    }, interval);
};

const finishCalibration = () => {
    isCalibrating.value = false;

    if (rawGazeBuffer.value.length === 0) {
        alert("데이터가 수집되지 않았습니다. 다시 시도해주세요.");
        return;
    }

    // Calculate Average
    let sumX = 0, sumY = 0;
    for (const p of rawGazeBuffer.value) {
        sumX += p.x;
        sumY += p.y;
    }
    const avgX = sumX / rawGazeBuffer.value.length;
    const avgY = sumY / rawGazeBuffer.value.length;

    // Emit average
    emit('calibrate', { x: avgX, y: avgY });

    // Close overlay
    isCalibrationMode.value = false;
};

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

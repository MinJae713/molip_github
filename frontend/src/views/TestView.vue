<template>
    <div class="ar-container">
        <video ref="videoRef" autoplay playsinline style="display: none;"></video>

        <canvas ref="threeCanvasRef"></canvas>

        <div v-if="isLoading" class="status-label">
            AI 모델 및 그래픽 엔진 로딩 중...
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import { FaceLandmarker, FilesetResolver } from '@mediapipe/tasks-vision';
import type { NormalizedLandmark } from '@mediapipe/tasks-vision';

// HTML 요소 참조
const videoRef = ref<HTMLVideoElement | null>(null);
const threeCanvasRef = ref<HTMLCanvasElement | null>(null);
const isLoading = ref(true);

// Three.js 관련 변수
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let videoTexture: THREE.VideoTexture;
let maskGroup: THREE.Group = new THREE.Group(); // 모델을 담을 그룹
let maskModel: THREE.Group | null = null; // 박스 대신 모델을 담을 변수

// MediaPipe 관련 변수
let faceLandmarker: FaceLandmarker;
let animationFrameId: number;

const initThreeJS = () => {
    // Scene, Camera, Renderer 초기화
    scene = new THREE.Scene();

    camera = new THREE.PerspectiveCamera(
        75,
        threeCanvasRef.value!.clientWidth / threeCanvasRef.value!.clientHeight,
        0.1,
        1000
    );
    camera.position.z = 5;

    renderer = new THREE.WebGLRenderer({
        canvas: threeCanvasRef.value!,
        alpha: true
    });
    renderer.setSize(threeCanvasRef.value!.clientWidth, threeCanvasRef.value!.clientHeight);

    // 비디오 텍스처 설정 (배경)
    if (videoRef.value) {
        videoTexture = new THREE.VideoTexture(videoRef.value);
        const videoMaterial = new THREE.MeshBasicMaterial({ map: videoTexture });
        const videoGeometry = new THREE.PlaneGeometry(8, 6);
        const videoMesh = new THREE.Mesh(videoGeometry, videoMaterial);
        videoMesh.position.z = -5;
        scene.add(videoMesh);
    }

    // 1. 조명 강화 (3D 모델은 조명이 있어야 보입니다)
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
    scene.add(ambientLight);
    const dirLight = new THREE.DirectionalLight(0xffffff, 1);
    dirLight.position.set(0, 5, 5);
    scene.add(dirLight);

    // 2. GLTF 모델 로더 설정
    const loader = new GLTFLoader();

    // 테스트용 무료 모델 URL (귀여운 여우 마스크 샘플)
    const modelUrl = 'https://raw.githack.com/KhronosGroup/glTF-Sample-Models/master/2.0/Duck/glTF-Binary/Duck.glb';
    // (임시로 오리 모델을 넣었습니다. 마스크용 모델이 있다면 그 URL을 넣으시면 됩니다.)

    loader.load(modelUrl, (gltf) => {
        maskModel = gltf.scene;
        maskModel.scale.set(0.5, 0.5, 0.5); // 모델 크기 조절
        scene.add(maskModel);
    });
};

/**
 * 1. Three.js 엔진 초기화
 */
/**
 * 2. MediaPipe Face Landmarker 설정
 */
const initMediaPipe = async () => {
    const filesetResolver = await FilesetResolver.forVisionTasks(
        "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@latest/wasm"
    );

    faceLandmarker = await FaceLandmarker.createFromOptions(filesetResolver, {
        baseOptions: {
            modelAssetPath: `https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task`,
            delegate: "GPU"
        },
        runningMode: "VIDEO",
        numFaces: 1
    });
};

/**
 * 3. 카메라 스트림 시작
 */
const startCamera = async () => {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            video: { width: 640, height: 480 }
        });
        if (videoRef.value) {
            videoRef.value.srcObject = stream;
        }
    } catch (err) {
        console.error("카메라를 시작할 수 없습니다:", err);
    }
};

/**
 * 4. 실시간 렌더링 루프 (AI 좌표 연동)
 */
const renderLoop = () => {
    if (videoRef.value && faceLandmarker) {
        const results = faceLandmarker.detectForVideo(videoRef.value, performance.now());

        if (results.faceLandmarks && results.faceLandmarks.length > 0) {
            const landmarks: NormalizedLandmark[] = results.faceLandmarks[0]!;

            // [핵심] 코 끝(Index 1) 좌표 추출
            const nose = landmarks[1];

            if (nose && maskGroup) {
                // MediaPipe(0~1) -> Three.js 공간 좌표 변환
                // X축은 카메라 반전 때문에 마이너스 처리
                maskGroup.position.x = -(nose.x - 0.5) * 8;
                maskGroup.position.y = -(nose.y - 0.5) * 6;
                maskGroup.position.z = -nose.z * 10;

                // 추가 팁: 얼굴 크기에 맞춰 박스 크기 조절 (미간 거리 기준)
                if (landmarks[33] && landmarks[263]) {
                    const eyeDist = Math.abs(landmarks[33].x - landmarks[263].x);
                    maskGroup.scale.setScalar(eyeDist * 5);
                }
            }
        }
    }

    renderer.render(scene, camera);
    animationFrameId = requestAnimationFrame(renderLoop);
};

onMounted(async () => {
    await initMediaPipe();
    await startCamera();
    initThreeJS();
    isLoading.value = false;
    renderLoop();
});

// 컴포넌트 해제 시 루프 중단
onUnmounted(() => {
    cancelAnimationFrame(animationFrameId);
    if (faceLandmarker) faceLandmarker.close();
});
</script>

<style scoped>
.ar-container {
    position: relative;
    width: 640px;
    height: 480px;
    margin: 20px auto;
    overflow: hidden;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

canvas {
    transform: scaleX(-1);
    /* 셀카 모드 반전 */
    width: 100%;
    height: 100%;
    display: block;
}

.status-label {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 15px 25px;
    border-radius: 30px;
    font-weight: bold;
}
</style>
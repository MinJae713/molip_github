<template>
    <!-- <div class="ar-container">
        <video ref="videoRef" autoplay playsinline style="display: none;"></video>

        <canvas ref="threeCanvasRef"></canvas>

        <div v-if="isLoading" class="status-label">
            AI 모델 및 그래픽 엔진 로딩 중...
        </div>
    </div> -->
</template>

<script setup lang="ts">
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
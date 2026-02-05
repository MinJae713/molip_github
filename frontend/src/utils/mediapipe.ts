import { FaceMesh, type Results as FaceMeshResults } from "@mediapipe/face_mesh";
import { Pose, type Results as PoseResults } from "@mediapipe/pose";
import { GazeTracker, type GazeResult } from "./gazeTracker";

export class MediaPipeManager {
    private faceMesh: FaceMesh | null = null;
    private pose: Pose | null = null;
    private gazeTracker: GazeTracker;

    private videoElement: HTMLVideoElement;
    private canvasElement: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D | null;
    private animationId: number | null = null;

    // Callbacks
    private onCropReady: (arrayBuffer: ArrayBuffer) => void;
    private onAnalysisReady?: (result: GazeResult) => void;

    // State
    private lastFaceResults: FaceMeshResults | null = null;
    private lastPoseResults: PoseResults | null = null;

    // Sending Interval
    private lastSentTime: number = 0;
    private readonly SEND_INTERVAL = 1000;

    constructor(
        videoElement: HTMLVideoElement,
        onCropReady: (arrayBuffer: ArrayBuffer) => void,
        previewCanvas?: HTMLCanvasElement,
        onAnalysisReady?: (result: GazeResult) => void
    ) {
        this.videoElement = videoElement;
        this.onCropReady = onCropReady;
        this.onAnalysisReady = onAnalysisReady;
        this.canvasElement = previewCanvas || document.createElement('canvas');
        this.ctx = this.canvasElement.getContext('2d');
        this.gazeTracker = new GazeTracker();
    }

    async init() {
        // 1. Initialize FaceMesh
        this.faceMesh = new FaceMesh({
            locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh@0.4.1633559619/${file}`
        });
        this.faceMesh.setOptions({
            maxNumFaces: 1,
            refineLandmarks: true,
            minDetectionConfidence: 0.5
        });
        this.faceMesh.onResults(this.onFaceResults.bind(this));

        // 2. Initialize Pose
        this.pose = new Pose({
            locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/pose@0.5.1675469404/${file}`
        });
        this.pose.setOptions({
            modelComplexity: 1,
            smoothLandmarks: true,
            minDetectionConfidence: 0.5,
            minTrackingConfidence: 0.5
        });
        this.pose.onResults(this.onPoseResults.bind(this));
    }

    async start(stream: MediaStream) {
        if (!this.faceMesh || !this.pose) await this.init();
        this.videoElement.srcObject = stream;

        await this.videoElement.play().catch(err => console.error("Video play error:", err));

        const loop = async () => {
            if (this.videoElement.paused || this.videoElement.ended) return;

            // Send to both models
            // Note: This might be heavy. For optimization, we could alternate or run in parallel.
            // running await sequentially for simplicity now.
            await this.faceMesh?.send({ image: this.videoElement });
            await this.pose?.send({ image: this.videoElement });

            this.performAnalysis();

            this.animationId = requestAnimationFrame(loop);
        }
        loop();
    }

    private onFaceResults(results: FaceMeshResults) {
        this.lastFaceResults = results;
    }

    private onPoseResults(results: PoseResults) {
        this.lastPoseResults = results;
    }

    private performAnalysis() {
        const now = Date.now();

        // 1. Gaze & Behavior Analysis
        const faceLandmarks = this.lastFaceResults?.multiFaceLandmarks?.[0] || [];
        const poseLandmarks = this.lastPoseResults?.poseLandmarks || null;

        // Note: modify gazeTracker.analyze to accept empty list for consistent return
        // Analyze every frame for smoothing filters to work
        const result = this.gazeTracker.analyze(faceLandmarks, poseLandmarks);

        if (this.onAnalysisReady) {
            this.onAnalysisReady(result);
        }

        // 2. Image Cropping (Throttle this one - heavy and for backend)
        if (now - this.lastSentTime >= this.SEND_INTERVAL) {
            if (faceLandmarks && faceLandmarks.length > 0 && this.ctx) {
                this.processCrop(faceLandmarks);
            }
            this.lastSentTime = now;
        }
    }

    private processCrop(landmarks: any[]) {
        if (!this.ctx) return;
        const width = this.videoElement.videoWidth;
        const height = this.videoElement.videoHeight;

        if (width === 0 || height === 0) return;

        // 1. Calculate Bounding Box
        let minX = width, minY = height, maxX = 0, maxY = 0;
        landmarks.forEach((lm: any) => {
            const x = lm.x * width; const y = lm.y * height;
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
        })

        // Padding
        const p = 0.2;
        const w = maxX - minX; const h = maxY - minY;
        minX = Math.max(0, minX - w * p); minY = Math.max(0, minY - h * p);
        const cropW = Math.min(width - minX, w * (1 + p * 2));
        const cropH = Math.min(height - minY, h * (1 + p * 2));

        // Draw
        this.canvasElement.width = cropW;
        this.canvasElement.height = cropH;
        this.ctx.drawImage(this.videoElement, minX, minY, cropW, cropH, 0, 0, cropW, cropH);

        // Blob Conversion
        this.canvasElement.toBlob(async (blob) => {
            if (blob) {
                const arrayBuffer = await blob.arrayBuffer();
                this.onCropReady(arrayBuffer);
                //  console.log(`[MediaPipe] Face cropped (${blob.size} bytes)`);
            }
        }, 'image/jpeg', 0.7);
    }

    stop() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
        this.faceMesh?.close();
        this.pose?.close();
        this.faceMesh = null;
        this.pose = null;
        this.videoElement.srcObject = null;
    }

    calibrate(targetX: number, targetY: number, currentRawX: number, currentRawY: number) {
        this.gazeTracker.calibrate(targetX, targetY, currentRawX, currentRawY);
    }
}
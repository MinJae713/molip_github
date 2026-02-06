import type { NormalizedLandmark, NormalizedLandmarkList } from '@mediapipe/face_mesh';

// Define structures that were implicit in JS
export interface MolipSessionData {
    timeline: string;           // "2026-01-30T16:30:00Z" 또는 타임스탬프
    isLookingAtMonitor: boolean; // 화면 내 존재 여부
    isStable: boolean;          // 머리와 몸이 모두 고정되었는지 여부 (몰입 여부)
    isHeadStable: boolean;      // 머리 고정 여부 (상세 분석용)
    isBodyStable: boolean;      // 몸 고정 여부 (상세 분석용)
    isAbsent: boolean;          // 자리비움 여부 (얼굴 미감지 시 true)
}

export interface Point {
    x: number;
    y: number;
    z?: number;
    visibility?: number;
}

export interface GazeResult {
    state: string;
    gaze: Point;
    irisSize: number;
    gain: number;
    rawGaze?: Point;
    isFaceDetected: boolean;
    isLookingAtMonitor: boolean;
    isHeadStable: boolean;
    isBodyStable: boolean;
    isStable: boolean;          // [NEW] 머리와 몸이 모두 고정되었는지 여부
    isAbsent: boolean;          // [NEW] 자리비움 여부
}

export class OneEuroFilter {
    minCutoff: number;
    beta: number;
    dCutoff: number;
    xPrev: number | null;
    dxPrev: number;
    tPrev: number | null;

    constructor(minCutoff: number = 1.0, beta: number = 0.0, dCutoff: number = 1.0) {
        this.minCutoff = minCutoff;
        this.beta = beta;
        this.dCutoff = dCutoff;
        this.xPrev = null;
        this.dxPrev = 0.0;
        this.tPrev = null;
    }

    smoothingFactor(t_e: number, cutoff: number): number {
        const r = 2 * Math.PI * cutoff * t_e;
        return r / (r + 1);
    }

    exponentialSmoothing(a: number, x: number, xPrev: number): number {
        return a * x + (1 - a) * xPrev;
    }

    filter(x: number, t: number): number {
        if (this.xPrev === null || this.tPrev === null) {
            this.xPrev = x;
            this.dxPrev = 0.0;
            this.tPrev = t;
            return x;
        }

        const t_e = t - this.tPrev;

        if (t_e <= 0.0) return this.xPrev;

        const a_d = this.smoothingFactor(t_e, this.dCutoff);
        const dx = (x - this.xPrev) / t_e;
        const dx_hat = this.exponentialSmoothing(a_d, dx, this.dxPrev);

        const cutoff = this.minCutoff + this.beta * Math.abs(dx_hat);
        const a = this.smoothingFactor(t_e, cutoff);
        const x_hat = this.exponentialSmoothing(a, x, this.xPrev);

        this.xPrev = x_hat;
        this.dxPrev = dx_hat;
        this.tPrev = t;

        return x_hat;
    }
}

export class GazeTracker {
    ROI_X_MIN: number;
    ROI_X_MAX: number;
    ROI_Y_MIN: number;
    ROI_Y_MAX: number;
    SENSITIVITY_X: number;
    SENSITIVITY_Y: number;
    Y_OFFSET_BIAS: number;
    REFERENCE_IRIS_SIZE: number;
    HEAD_STD_THRESHOLD: number;
    filterX: OneEuroFilter;
    filterY: OneEuroFilter;
    headBuffer: Point[];
    bufferSize: number;
    stateHistory: string[];
    MAX_STATE_HISTORY: number;
    bodyBuffer: { l: Point; r: Point }[];
    BODY_BUFFER_SIZE: number;
    BODY_STD_THRESHOLD: number;
    calibrationOffset: { x: number; y: number };

    constructor() {
        // Constants (Matching Python config)
        this.ROI_X_MIN = 0.35;
        this.ROI_X_MAX = 0.65;
        this.ROI_Y_MIN = 0.35;
        this.ROI_Y_MAX = 0.65;

        this.SENSITIVITY_X = 3.5;
        this.SENSITIVITY_Y = 4.5;
        this.Y_OFFSET_BIAS = 0.08;
        this.REFERENCE_IRIS_SIZE = 0.035;
        this.HEAD_STD_THRESHOLD = 0.01;

        // Smoothing Tuning (More stable when still, responsive when moving)
        // User requested smoother tracking -> Decrease beta significantly
        this.filterX = new OneEuroFilter(0.01, 0.1);
        this.filterY = new OneEuroFilter(0.01, 0.1);

        // Head buffer
        this.headBuffer = [];
        this.bufferSize = 20;

        // Hysteresis Buffer
        this.stateHistory = [];
        this.MAX_STATE_HISTORY = 7;

        // [NEW] Body (Shoulder) Buffer for Immersion
        this.bodyBuffer = [];
        this.BODY_BUFFER_SIZE = 30; // ~1 second @ 30fps
        this.BODY_STD_THRESHOLD = 0.005; // Tunable

        // [NEW] Calibration Offset
        this.calibrationOffset = { x: 0, y: 0 };
    }

    _getLandmarkPoint(landmarks: NormalizedLandmarkList, idx: number): Point {
        const lm = landmarks[idx];
        if (!lm) return { x: 0, y: 0 };
        return { x: lm.x, y: lm.y, z: lm.z, visibility: lm.visibility };
    }

    _distance(p1: Point, p2: Point): number {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    _calculateIrisDiameter(landmarks: NormalizedLandmarkList, centerIdx: number, radiusIndices: number[]): number {
        const center = this._getLandmarkPoint(landmarks, centerIdx);
        let sumRadii = 0;

        for (const idx of radiusIndices) {
            sumRadii += this._distance(this._getLandmarkPoint(landmarks, idx), center);
        }

        return (sumRadii / radiusIndices.length) * 2.0;
    }

    _calculateRelativeGaze(irisCenter: Point, eyeCorners: Point[], gain: number = 1.0): Point {
        const p1 = eyeCorners[0];
        const p2 = eyeCorners[1];
        if (!p1 || !p2) return { x: 0.5, y: 0.5 };

        const eyeWidth = this._distance(p1, p2);
        if (eyeWidth === 0) return { x: 0.5, y: 0.5 };

        const eyeCx = (p1.x + p2.x) / 2.0;
        const eyeCy = (p1.y + p2.y) / 2.0;

        let dx = (irisCenter.x - eyeCx) / eyeWidth;
        let dy = (irisCenter.y - eyeCy) / eyeWidth;

        dy *= 2.0;
        dy += this.Y_OFFSET_BIAS;

        const dxAmp = dx * gain * this.SENSITIVITY_X;
        const dyAmp = dy * gain * this.SENSITIVITY_Y;

        // 0.5 + dxAmp -> 0.5 - dxAmp (Flip X axis)
        return {
            x: 0.5 - dxAmp,
            y: 0.5 + dyAmp
        };
    }

    _calculateHeadYaw(landmarks: NormalizedLandmarkList): number {
        // Simple 2D projection logic for Yaw
        const nose = this._getLandmarkPoint(landmarks, 1);
        const leftSide = this._getLandmarkPoint(landmarks, 234); // Left Ear area
        const rightSide = this._getLandmarkPoint(landmarks, 454); // Right Ear area

        const distLeft = Math.abs(nose.x - leftSide.x);
        const distRight = Math.abs(nose.x - rightSide.x);

        const totalWidth = distLeft + distRight;
        if (totalWidth === 0) return 0;

        const yawRatio = (distLeft / totalWidth) - 0.5;
        return yawRatio * 2.0; // Gain for Yaw
    }

    _getStabilizedState(newState: string): string {
        this.stateHistory.push(newState);
        if (this.stateHistory.length > this.MAX_STATE_HISTORY) {
            this.stateHistory.shift();
        }

        // Voting
        const counts: { [key: string]: number } = {};
        let maxCount = 0;
        let majorityState = newState;

        for (const s of this.stateHistory) {
            counts[s] = (counts[s] || 0) + 1;
            if (counts[s] > maxCount) {
                maxCount = counts[s];
                majorityState = s;
            }
        }

        return majorityState;
    }

    _calculateEAR(landmarks: NormalizedLandmarkList, indices: number[]): number {
        const p1 = landmarks[indices[1]];
        const p2 = landmarks[indices[5]];
        const p3 = landmarks[indices[2]];
        const p4 = landmarks[indices[4]];
        const p0 = landmarks[indices[0]];
        const p5 = landmarks[indices[3]];

        if (!p1 || !p2 || !p3 || !p4 || !p0 || !p5) return 0.0;

        // Vertical
        const v1 = this._distance({ x: p1.x, y: p1.y }, { x: p2.x, y: p2.y });
        const v2 = this._distance({ x: p3.x, y: p3.y }, { x: p4.x, y: p4.y });
        // Horizontal
        const h = this._distance({ x: p0.x, y: p0.y }, { x: p5.x, y: p5.y });

        if (h === 0) return 0.0;
        return (v1 + v2) / (2.0 * h);
    }

    calibrate(targetX: number, targetY: number, currentRawX: number, currentRawY: number): void {
        this.calibrationOffset.x = targetX - currentRawX;
        this.calibrationOffset.y = targetY - currentRawY;
        console.log(`Calibrated! Offset: ${this.calibrationOffset.x.toFixed(3)}, ${this.calibrationOffset.y.toFixed(3)}`);
    }

    _isBodyStable(poseLandmarks: NormalizedLandmarkList): boolean {
        if (!poseLandmarks || poseLandmarks.length <= 12) return false;

        const leftShoulder = poseLandmarks[11];
        const rightShoulder = poseLandmarks[12];

        if (!leftShoulder || !rightShoulder) return false;

        // Construct points compatible with our Point interface
        const lPoint: Point = { x: leftShoulder.x, y: leftShoulder.y, z: leftShoulder.z, visibility: leftShoulder.visibility };
        const rPoint: Point = { x: rightShoulder.x, y: rightShoulder.y, z: rightShoulder.z, visibility: rightShoulder.visibility };

        this.bodyBuffer.push({ l: lPoint, r: rPoint });
        if (this.bodyBuffer.length > this.BODY_BUFFER_SIZE) {
            this.bodyBuffer.shift();
        }

        if (this.bodyBuffer.length < this.BODY_BUFFER_SIZE / 2) return false;

        let sumLx = 0, sumLy = 0, sumRx = 0, sumRy = 0;
        for (let b of this.bodyBuffer) {
            sumLx += b.l.x; sumLy += b.l.y;
            sumRx += b.r.x; sumRy += b.r.y;
        }
        const len = this.bodyBuffer.length;
        const mLx = sumLx / len; const mLy = sumLy / len;
        const mRx = sumRx / len; const mRy = sumRy / len;

        let varSum = 0;
        for (let b of this.bodyBuffer) {
            varSum += Math.pow(b.l.x - mLx, 2) + Math.pow(b.l.y - mLy, 2) +
                Math.pow(b.r.x - mRx, 2) + Math.pow(b.r.y - mRy, 2);
        }

        const avgVar = varSum / (len * 2);
        const std = Math.sqrt(avgVar);

        return std < this.BODY_STD_THRESHOLD;
    }

    analyze(faceLandmarks: NormalizedLandmarkList, poseLandmarks: NormalizedLandmarkList | null): GazeResult {
        if (!faceLandmarks || faceLandmarks.length === 0) {
            return {
                state: "No Face",
                gaze: { x: 0.5, y: 0.5 },
                irisSize: 0,
                gain: 1.0,
                isFaceDetected: false,
                isLookingAtMonitor: false,
                isHeadStable: false,
                isBodyStable: false,
                isStable: false,
                isAbsent: true
            };
        }

        const timestamp = Date.now() / 1000.0;

        // 1. Blink Detection
        const leftEar = this._calculateEAR(faceLandmarks, [33, 160, 158, 133, 153, 144]);
        const rightEar = this._calculateEAR(faceLandmarks, [362, 385, 387, 263, 373, 380]);
        const avgEar = (leftEar + rightEar) / 2.0;

        if (avgEar < 0.22) {
            return {
                state: "Blinking",
                gaze: {
                    x: this.filterX.xPrev !== null ? this.filterX.xPrev : 0.5,
                    y: this.filterY.xPrev !== null ? this.filterY.xPrev : 0.5
                },
                irisSize: 0,
                gain: 1.0,
                rawGaze: { x: 0.5, y: 0.5 },
                isFaceDetected: true,
                isLookingAtMonitor: true,
                isHeadStable: true,
                isBodyStable: true,
                isStable: true, // Blinking implies relatively stable typically, or keep previous. Let's assume stable.
                isAbsent: false
            };
        }

        // 2. Iris Calculation
        const leftIrisCenter = this._getLandmarkPoint(faceLandmarks, 468);
        const rightIrisCenter = this._getLandmarkPoint(faceLandmarks, 473);

        const leftDiameter = this._calculateIrisDiameter(faceLandmarks, 468, [469, 470, 471, 472]);
        const rightDiameter = this._calculateIrisDiameter(faceLandmarks, 473, [474, 475, 476, 477]);
        const avgIrisDiameter = (leftDiameter + rightDiameter) / 2.0;

        let distanceFactor = 1.0;
        if (avgIrisDiameter > 0) {
            distanceFactor = this.REFERENCE_IRIS_SIZE / avgIrisDiameter;
        }
        const finalGain = Math.max(0.5, Math.min(distanceFactor, 4.0));

        // 3. Gaze Logic
        const leftInner = this._getLandmarkPoint(faceLandmarks, 33);
        const leftOuter = this._getLandmarkPoint(faceLandmarks, 133);
        const rightInner = this._getLandmarkPoint(faceLandmarks, 362);
        const rightOuter = this._getLandmarkPoint(faceLandmarks, 263);

        const lGaze = this._calculateRelativeGaze(leftIrisCenter, [leftInner, leftOuter], finalGain);
        const rGaze = this._calculateRelativeGaze(rightIrisCenter, [rightInner, rightOuter], finalGain);

        let rawX = (lGaze.x + rGaze.x) / 2.0;
        let rawY = (lGaze.y + rGaze.y) / 2.0;

        const headYaw = this._calculateHeadYaw(faceLandmarks);
        rawX += headYaw;

        const uncalibratedRawX = rawX;
        const uncalibratedRawY = rawY;

        rawX += this.calibrationOffset.x;
        rawY += this.calibrationOffset.y;

        // 4. Smoothing
        const smoothX = this.filterX.filter(rawX, timestamp);
        const smoothY = this.filterY.filter(rawY, timestamp);

        // 5. Head Stability
        const noseTip = this._getLandmarkPoint(faceLandmarks, 1);
        this.headBuffer.push(noseTip);
        if (this.headBuffer.length > this.bufferSize) {
            this.headBuffer.shift();
        }

        let isHeadStable = true;
        if (this.headBuffer.length > 2) {
            let sumX = 0, sumY = 0, sumZ = 0;
            // z might be undefined in Point but present in Landmark. 
            // In our _getLandmarkPoint, we attach z.
            for (let p of this.headBuffer) { sumX += p.x; sumY += p.y; sumZ += (p.z || 0); }
            const meanX = sumX / this.headBuffer.length;
            const meanY = sumY / this.headBuffer.length;
            const meanZ = sumZ / this.headBuffer.length;

            let mse = 0;
            for (let p of this.headBuffer) {
                mse += Math.pow(p.x - meanX, 2) + Math.pow(p.y - meanY, 2) + Math.pow((p.z || 0) - meanZ, 2);
            }
            const std = Math.sqrt(mse / this.headBuffer.length);
            isHeadStable = std < this.HEAD_STD_THRESHOLD;
        }

        // Body Stability
        let isBodyStable = false;
        if (poseLandmarks && poseLandmarks.length > 0) {
            isBodyStable = this._isBodyStable(poseLandmarks);
        }

        // 6. State
        const isLookingAtMonitor = (
            smoothX >= 0.2 && smoothX <= 0.8 &&
            smoothY >= 0.1 && smoothY <= 0.9
        );

        let currentState = "Distracted";
        // Immersion if all stable and looking at monitor.
        // Actually, let's keep the logic simple:
        const isStable = isHeadStable && isBodyStable;

        if (isLookingAtMonitor) {
            if (isStable) {
                currentState = "Immersion";
            } else {
                currentState = "Concentration";
            }
        }

        const stableState = this._getStabilizedState(currentState);

        return {
            state: stableState,
            gaze: { x: smoothX, y: smoothY },
            irisSize: avgIrisDiameter,
            gain: finalGain,
            rawGaze: { x: uncalibratedRawX, y: uncalibratedRawY },
            isFaceDetected: true,
            isLookingAtMonitor: isLookingAtMonitor,
            isHeadStable: isHeadStable,
            isBodyStable: isBodyStable,
            isStable: isStable,
            isAbsent: false
        };
    }
}

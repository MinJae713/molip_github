import { ref, computed, toRaw } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import adapter from "webrtc-adapter";
import JanusPkg from "janus-gateway";
import { useWorkspaceStore } from "@/stores/workspace";
import { useUserStore } from "@/stores/user";
import type { Participant } from "@/types/live";
import Stomp from "stompjs";
import SockJS from "sockjs-client";

// ========================================
// Janus 전역 설정
// ========================================
if (typeof window !== "undefined") {
  (window as any).adapter = adapter;
}
const Janus = JanusPkg?.Janus || (JanusPkg as any);

// ========================================
// 화상회의 API 클라이언트
// ========================================
const videoApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
});

// API 요청 시 JWT 토큰을 헤더에 자동으로 추가
videoApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ========================================
// TypeScript 타입 정의
// ========================================

// WebRTC SDP (Session Description Protocol) 정보
interface JSEP {
  type: string; // "offer" | "answer"
  sdp: string; // SDP 내용
}

// Janus 서버로부터 받는 메시지 형식
interface JanusMessage {
  videoroom?: string; // 이벤트 타입 ("joined", "event" 등)
  room?: number; // 방 ID
  publishers?: Publisher[]; // 현재 방에 있는 퍼블리셔(학생) 목록
  leaving?: number; // 퇴장한 사용자의 Feed ID
  unpublished?: number; // 스트림을 중단한 사용자의 Feed ID
  error?: string; // 에러 메시지
  [key: string]: any; // 기타 동적 속성
}

// Publisher: Janus 방에서 미디어를 송출하는 사용자
export interface Publisher {
  id: number; // Feed ID (Janus가 할당하는 고유 번호)
  display?: string; // 표시 이름 (예: "memberId|[학생] 홍길동")
  audio_codec?: string;
  video_codec?: string;
  talking?: boolean;
}

// Janus 플러그인 핸들: Janus와의 통신 채널
interface JanusHandle {
  plugin: string; // 플러그인 이름 (예: "janus.plugin.videoroom")
  id: number; // 핸들 ID

  // Janus에 메시지 전송
  send(params: {
    message: any; // 전송할 메시지 (예: join, configure, subscribe 등)
    jsep?: JSEP; // SDP 정보 (옵션)
    success?: (data?: any) => void;
    error?: (err: any) => void;
  }): void;

  // WebRTC Offer 생성 (미디어 송출용)
  createOffer(params: {
    tracks?: Array<{ type: string; capture: any; recv?: boolean }>; // 송출할 트랙 목록
    media?: {
      audioRecv?: boolean;
      videoRecv?: boolean;
      audioSend?: boolean;
      videoSend?: boolean;
    };
    success: (jsep: JSEP) => void;
    error: (err: any) => void;
  }): void;

  // WebRTC Answer 생성 (미디어 수신용)
  createAnswer(params: any): void;
  // 원격 SDP 처리
  handleRemoteJsep(params: { jsep: JSEP }): void;
  // 플러그인 핸들 해제
  detach(params?: any): void;
}

// ========================================
// Pinia Store: 트레이너 라이브 세션 관리
// ========================================
export const useTrainerLiveStore = defineStore("trainerLive", () => {
  const userStore = useUserStore();

  // ========================================
  // State: 상태 관리
  // ========================================

  // --- Janus 연결 관련 ---
  const janus = ref<any>(null); // Janus Gateway 인스턴스
  const roomHandle = ref<JanusHandle | null>(null); // 강사용 플러그인 핸들 (publish용)
  const roomInfo = ref<any>(null); // 방 정보 (janusRoomId, pin)
  const connStatus = ref("Ready"); // 연결 상태 ("Ready", "Connected", "Error" 등)
  const opaqueId = ref(`trainer-${Janus.randomString(12)}`); // 클라이언트 고유 ID

  // --- 세션 정보 ---
  const myId = ref<number | null>(null); // 강사의 Janus Feed ID (방 입장 시 할당됨)
  const sessionId = ref<string | number>(""); // 라이브 세션 ID (DB의 세션 ID)
  const isSessionStarted = ref(false); // Janus 세션(화면 공유)이 시작되었는지 여부
  const isAnalysisStarted = ref(false); // AI 분석(음성 분석 및 리포트 기록)이 시작되었는지 여부

  // --- 스트림 관리 ---
  const screenStream = ref<MediaStream | null>(null); // 화면 공유 스트림
  const micStream = ref<MediaStream | null>(null); // 마이크 스트림
  const voiceSocket = ref<WebSocket | null>(null); // 음성 분석용 WebSocket
  const mediaRecorder = ref<MediaRecorder | null>(null); // MediaRecorder

  // --- 시간 동기화 ---
  const classStartTime = ref<number>(0); // 수업 시작 시간 (서버 timestamp, ms)
  const timeOffset = ref<number>(0); // 클라이언트-서버 시간 차이 (ms), 학생과 동일한 로직

  // --- 세션 상태 ---

  // --- 쉬는 시간 관리 ---
  const isOnBreak = ref(false); // 현재 쉬는 시간 여부
  const pauseHistory = ref<{ startTime: number; endTime: number }[]>([]); // 쉬는 시간 이력
  const currentPauseStart = ref<number | null>(null); // 현재 쉬는 시간 시작 시점 (timestamp)
  // --- 스트림 관리 ---
  const screenStream = ref<MediaStream | null>(null); // 화면 공유 스트림
  const micStream = ref<MediaStream | null>(null); // 마이크 스트림
  const voiceSocket = ref<WebSocket | null>(null); // 음성 분석용 WebSocket
  const mediaRecorder = ref<MediaRecorder | null>(null); // MediaRecorder

  // --- 시간 동기화 ---
  const classStartTime = ref<number>(0); // 수업 시작 시간 (서버 timestamp, ms)
  const timeOffset = ref<number>(0); // 클라이언트-서버 시간 차이 (ms), 학생과 동일한 로직

  // --- 세션 상태 ---

  // --- 쉬는 시간 관리 ---
  const isOnBreak = ref(false); // 현재 쉬는 시간 여부
  const pauseHistory = ref<{ startTime: number; endTime: number }[]>([]); // 쉬는 시간 이력
  const currentPauseStart = ref<number | null>(null); // 현재 쉬는 시간 시작 시점 (timestamp)

  // --- 학생 관리 ---
  const participants = ref<Participant[]>([]); // 입장한 학생 목록 (UI 표시용)
  const remoteFeeds = ref<Map<number, JanusHandle>>(new Map()); // 학생별 구독 핸들 (feedId -> Handle)
  const studentStreams = ref<Map<string, MediaStream>>(new Map()); // 학생별 비디오 스트림 (participantId -> Stream)
  const selectedStudentId = ref<string | null>(null); // PIP(Picture-in-Picture)에 표시할 학생 ID

  // --- 채팅/질문/퀴즈 관리 (WebSocket - 스토어에서 통합 관리) ---
  // --- 채팅/질문/퀴즈 관리 (WebSocket - 스토어에서 통합 관리) ---
  const chatList = ref<any[]>([]); // 채팅 메시지 목록
  const qaList = ref<any[]>([]); // 질문/답변 메시지 목록
  const quizData = ref<any>(null); // 퀴즈 데이터

  // --- 세션 생명주기 관리 ---
  async function openSession(id: string | number) {
    try {
      console.log(`[Session] Opening session: ${id}`);
      sessionId.value = id; // ✅ sessionId 설정 (STOMP 구독에 필요)

      await videoApi.post(`/session/start/${id}`);
      console.log("[Session] Session Start API Called");

      // ✅ STOMP 연결을 여기서 시작 (학생 입장 추적을 위해)
      console.log("[Session] Connecting to STOMP for participant tracking...");
      handleConnectWs();
    } catch (e) {
      console.error("[Session] Open Error", e);
    }
  }

  async function closeSession(id: string | number) {
    try {
      console.log(`[Session] Closing session: ${id}`);
      await videoApi.post(`/session/end/${id}`);
      console.log("[Session] Session End API Called");
    } catch (e) {
      console.error("[Session] Close Error", e);
    }
  }
  const quizData = ref<any>(null); // 퀴즈 데이터

  // --- 세션 생명주기 관리 ---
  async function openSession(id: string | number) {
    try {
      console.log(`[Session] Opening session: ${id}`);
      sessionId.value = id; // ✅ sessionId 설정 (STOMP 구독에 필요)

      await videoApi.post(`/session/start/${id}`);
      console.log("[Session] Session Start API Called");

      // ✅ STOMP 연결을 여기서 시작 (학생 입장 추적을 위해)
      console.log("[Session] Connecting to STOMP for participant tracking...");
      handleConnectWs();
    } catch (e) {
      console.error("[Session] Open Error", e);
    }
  }

  async function closeSession(id: string | number) {
    try {
      console.log(`[Session] Closing session: ${id}`);
      await videoApi.post(`/session/end/${id}`);
      console.log("[Session] Session End API Called");
    } catch (e) {
      console.error("[Session] Close Error", e);
    }
  }

  // ========================================
  // Actions: 메서드
  // ========================================

  /**
   * 1. Janus 초기화 및 방 정보 조회/생성
   * - 먼저 기존 방 정보 조회 시도
   * - 방이 없으면 새로 생성
   * - Janus Gateway에 연결
   */
  async function initJanus(id: string | number) {
    sessionId.value = id;
    try {
      // 1. 먼저 기존 방 정보 조회 시도
      try {
        const res = await videoApi.get(`/session/${id}/connection`);
        roomInfo.value = {
          janusRoomId: res.data.janusRoomId,
          pin: res.data.pin,
        };
        console.log("기존 방 정보 조회 성공:", roomInfo.value);
      } catch (connectionError: any) {
        // 2. 방이 없으면 새로 생성
        console.log("기존 방 없음, 새 방 생성 시도...");
        const createRes = await videoApi.post(`/session/${id}/start`);
        roomInfo.value = {
          janusRoomId: createRes.data.janusRoomId,
          pin: createRes.data.pin,
        };
        console.log("새 방 생성 성공:", roomInfo.value);
      }
    } catch (e) {
      console.error("방 정보 조회/생성 실패", e);
      connStatus.value = "Session Info Error";
      return;
    }

    // Janus 라이브러리 초기화 및 연결 대기
    return new Promise<void>((resolve, reject) => {
      Janus.init({
        debug: "all", // 디버그 로그 활성화
        callback: () => {
          // WebSocket URL 구성 (https면 wss, http면 ws)
          const protocol =
            window.location.protocol === "https:" ? "wss://" : "ws://";
          const envUrl = import.meta.env.VITE_JANUS_URL;
          const serverUrl =
            envUrl || `${protocol}${window.location.hostname}/janus_ws`;
    // Janus 라이브러리 초기화 및 연결 대기
    return new Promise<void>((resolve, reject) => {
      Janus.init({
        debug: "all", // 디버그 로그 활성화
        callback: () => {
          // WebSocket URL 구성 (https면 wss, http면 ws)
          const protocol =
            window.location.protocol === "https:" ? "wss://" : "ws://";
          const envUrl = import.meta.env.VITE_JANUS_URL;
          const serverUrl =
            envUrl || `${protocol}${window.location.hostname}/janus_ws`;

          // JWT 토큰을 쿼리 파라미터로 추가
          const token = localStorage.getItem("accessToken");
          let finalServer = `${serverUrl}?session=${id}`;
          if (token) {
            finalServer += `&auth=${token}`;
          }
          // JWT 토큰을 쿼리 파라미터로 추가
          const token = localStorage.getItem("accessToken");
          let finalServer = `${serverUrl}?session=${id}`;
          if (token) {
            finalServer += `&auth=${token}`;
          }

          // Janus 인스턴스 생성 및 연결
          janus.value = new Janus({
            server: finalServer,
            iceServers: [{ urls: "stun:stun.l.google.com:19302" }], // STUN 서버 (NAT 통과용)
            success: async () => {
              connStatus.value = "Connected";

              // Plugin 부착을 기다려야 함
              try {
                await attachPlugin();
                resolve(); // 모든 초기화 완료
              } catch (e) {
                reject(e);
              }
            },
            error: (err: any) => {
              console.error("Janus Connect Error", err);
              connStatus.value = "Connection Error";
              reject(err);
            },
          });
        },
      });
          // Janus 인스턴스 생성 및 연결
          janus.value = new Janus({
            server: finalServer,
            iceServers: [{ urls: "stun:stun.l.google.com:19302" }], // STUN 서버 (NAT 통과용)
            success: async () => {
              connStatus.value = "Connected";

              // Plugin 부착을 기다려야 함
              try {
                await attachPlugin();
                resolve(); // 모든 초기화 완료
              } catch (e) {
                reject(e);
              }
            },
            error: (err: any) => {
              console.error("Janus Connect Error", err);
              connStatus.value = "Connection Error";
              reject(err);
            },
          });
        },
      });
    });
  }

  /**
   * 2. VideoRoom 플러그인 부착
   * - Janus의 VideoRoom 플러그인에 연결
   * - 강사용 핸들 생성 (publish용)
   */
  /**
   * 2. VideoRoom 플러그인 부착
   * - Janus의 VideoRoom 플러그인에 연결
   * - 강사용 핸들 생성 (publish용)
   */
  function attachPlugin(): Promise<void> {
    return new Promise((resolve, reject) => {
      janus.value.attach({
        plugin: "janus.plugin.videoroom", // VideoRoom 플러그인
        opaqueId: opaqueId.value, // 클라이언트 식별자
        success: (handle: JanusHandle) => {
          roomHandle.value = handle; // 핸들 저장
          joinRoom(); // 방 입장
          resolve();
        },
        error: (err: any) => {
          console.error("플러그인 부착 오류", err);
          reject(err);
        },
        onmessage: (msg: JanusMessage, jsep: JSEP) => onMessage(msg, jsep), // 메시지 수신
        onlocaltrack: (track: MediaStreamTrack, on: boolean) => {
          // 로컬 트랙 (강사의 화면 공유) 수신
          if (on && track.kind === "video") {
            screenStream.value = new MediaStream([track]);
          }
        },
        oncleanup: () => {
          // 정리 작업 (stopClass에서 처리)
        },
      });
  /**
   * 2. VideoRoom 플러그인 부착
   * - Janus의 VideoRoom 플러그인에 연결
   * - 강사용 핸들 생성 (publish용)
   */
  function attachPlugin(): Promise<void> {
    return new Promise((resolve, reject) => {
      janus.value.attach({
        plugin: "janus.plugin.videoroom", // VideoRoom 플러그인
        opaqueId: opaqueId.value, // 클라이언트 식별자
        success: (handle: JanusHandle) => {
          roomHandle.value = handle; // 핸들 저장
          joinRoom(); // 방 입장
          resolve();
        },
        error: (err: any) => {
          console.error("플러그인 부착 오류", err);
          reject(err);
        },
        onmessage: (msg: JanusMessage, jsep: JSEP) => onMessage(msg, jsep), // 메시지 수신
        onlocaltrack: (track: MediaStreamTrack, on: boolean) => {
          // 로컬 트랙 (강사의 화면 공유) 수신
          if (on && track.kind === "video") {
            screenStream.value = new MediaStream([track]);
          }
        },
        oncleanup: () => {
          // 정리 작업 (stopClass에서 처리)
        },
      });
    });
  }

  /**
   * 3. VideoRoom 방 입장
   * - publisher(송출자)로 방에 입장
   */
  function joinRoom() {
    if (!roomHandle.value || !roomInfo.value) return;

    const displayName = `[선생] 강사`; // 표시 이름
    const register = {
      request: "join", // 입장 요청
      room: roomInfo.value.janusRoomId, // 방 ID
      ptype: "publisher", // 타입: 송출자
      display: displayName, // 표시 이름
      pin: roomInfo.value.pin, // 방 PIN
    };
    roomHandle.value.send({ message: register });
  }

  /**
   * Janus 메시지 처리
   * - joined: 방 입장 완료
   * - event: 학생 입장/퇴장 등 이벤트
   */
  function onMessage(msg: JanusMessage, jsep: JSEP) {
    const event = msg["videoroom"];

    if (event === "joined") {
      // 방 입장 성공
      myId.value = msg["id"]; // 강사의 Feed ID 저장
      if (msg["publishers"]) {
        // 이미 입장한 학생들 처리
        handleIncomingStudents(msg["publishers"]);
      }
    } else if (event === "event") {
      // ✅ Janus 이벤트로 입장/퇴장 처리 (STOMP 백업)
      // ✅ Janus 이벤트로 입장/퇴장 처리 (STOMP 백업)
      if (msg["publishers"]) {
        console.log("[Janus Event] New publishers:", msg["publishers"]);
        console.log("[Janus Event] New publishers:", msg["publishers"]);
        handleIncomingStudents(msg["publishers"]);
      }

      // ✅ leaving/unpublished 이벤트 처리 (STOMP LEAVE가 안 올 때 대비)
      if (msg["leaving"] || msg["unpublished"]) {
        const feedId = msg["leaving"] || msg["unpublished"];
        console.log(
          "[Janus Event] Student leaving/unpublished feedId:",
          feedId,
        );
        if (feedId) {
          removeStudent(feedId);
        }
      }

      // ✅ leaving/unpublished 이벤트 처리 (STOMP LEAVE가 안 올 때 대비)
      if (msg["leaving"] || msg["unpublished"]) {
        const feedId = msg["leaving"] || msg["unpublished"];
        console.log(
          "[Janus Event] Student leaving/unpublished feedId:",
          feedId,
        );
        if (feedId) {
          removeStudent(feedId);
        }
      }
    }

    // SDP 처리 (WebRTC 협상)
    if (jsep && roomHandle.value) {
      roomHandle.value.handleRemoteJsep({ jsep });
    }
  }

  /**
   * 4. 강의 세션 시작 (화면 공유 및 마이크 스트림 송출)
   * - Janus에 미디어를 송출하여 학생들이 볼 수 있게 함
   * - AI 분석은 아직 시작하지 않음
   */
  async function startSession(): Promise<void> {
    return new Promise(async (resolve, reject) => {
      try {
        console.log("[startSession] 1. 화면 공유 스트림 요청 중...");
        // 1. 화면 공유 스트림 획득
        const displayStream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true,
        });
        const videoTrack = displayStream.getVideoTracks()[0];
        if (!videoTrack) {
          throw new Error("비디오 트랙을 찾을 수 없습니다");
        }
        console.log("[startSession] 2. 화면 공유 스트림 획득 완료");
  async function startSession(): Promise<void> {
    return new Promise(async (resolve, reject) => {
      try {
        console.log("[startSession] 1. 화면 공유 스트림 요청 중...");
        // 1. 화면 공유 스트림 획득
        const displayStream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true,
        });
        const videoTrack = displayStream.getVideoTracks()[0];
        if (!videoTrack) {
          throw new Error("비디오 트랙을 찾을 수 없습니다");
        }
        console.log("[startSession] 2. 화면 공유 스트림 획득 완료");

        // 2. 마이크 스트림 획득 (강사 음성)
        const userMicStream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });
        const audioTrack = userMicStream.getAudioTracks()[0];
        micStream.value = userMicStream;
        console.log(
          "[startSession] 3. 마이크 스트림 획득 완료. micStream:",
          !!micStream.value,
        );

        // [Bugfix] 로컬 미리보기를 위해 screenStream 상태 즉시 업데이트
        screenStream.value = displayStream;

        if (!roomHandle.value) {
          reject(new Error("roomHandle이 없습니다"));
          return;
        }

        console.log("[startSession] 4. Janus createOffer 호출 중...");
        // 3. Janus에 미디어 송출
        roomHandle.value.createOffer({
          tracks: [
            { type: "video", capture: videoTrack, recv: false },
            { type: "audio", capture: audioTrack, recv: false },
          ],
          success: async (jsep: JSEP) => {
            console.log("[startSession] 5. Janus Offer 생성 성공");
            const publish = { request: "configure", audio: true, video: true };
            roomHandle.value!.send({ message: publish, jsep });
        // 2. 마이크 스트림 획득 (강사 음성)
        const userMicStream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });
        const audioTrack = userMicStream.getAudioTracks()[0];
        micStream.value = userMicStream;
        console.log(
          "[startSession] 3. 마이크 스트림 획득 완료. micStream:",
          !!micStream.value,
        );

        // [Bugfix] 로컬 미리보기를 위해 screenStream 상태 즉시 업데이트
        screenStream.value = displayStream;

        if (!roomHandle.value) {
          reject(new Error("roomHandle이 없습니다"));
          return;
        }

        console.log("[startSession] 4. Janus createOffer 호출 중...");
        // 3. Janus에 미디어 송출
        roomHandle.value.createOffer({
          tracks: [
            { type: "video", capture: videoTrack, recv: false },
            { type: "audio", capture: audioTrack, recv: false },
          ],
          success: async (jsep: JSEP) => {
            console.log("[startSession] 5. Janus Offer 생성 성공");
            const publish = { request: "configure", audio: true, video: true };
            roomHandle.value!.send({ message: publish, jsep });

            // ✅ STOMP 연결은 openSession()에서 이미 완료됨
            isSessionStarted.value = true;
            console.log(
              "[startSession] 6. 세션 시작 완료. isSessionStarted:",
              isSessionStarted.value,
            );
            resolve(); // Promise 완료
          },
          error: (err: any) => {
            console.error("화면 공유 오퍼 생성 실패", err);
            reject(err);
          },
        });
            // ✅ STOMP 연결은 openSession()에서 이미 완료됨
            isSessionStarted.value = true;
            console.log(
              "[startSession] 6. 세션 시작 완료. isSessionStarted:",
              isSessionStarted.value,
            );
            resolve(); // Promise 완료
          },
          error: (err: any) => {
            console.error("화면 공유 오퍼 생성 실패", err);
            reject(err);
          },
        });

        // 화면 공유가 사용자에 의해 중지되면 수업 종료
        videoTrack.onended = () => stopClass();
      } catch (e) {
        console.error("장치 권한 오류", e);
        alert("화면 공유 및 마이크 권한이 필요합니다.");
        reject(e);
      }
    });
        // 화면 공유가 사용자에 의해 중지되면 수업 종료
        videoTrack.onended = () => stopClass();
      } catch (e) {
        console.error("장치 권한 오류", e);
        alert("화면 공유 및 마이크 권한이 필요합니다.");
        reject(e);
      }
    });
  }

  /**
   * 4-1. AI 분석 시작
   * - 백엔드 분석 API 호출 및 음성 데이터 전송 시작
   */
  async function startAnalysis() {
    console.log(
      "[startAnalysis] 호출됨. sessionId:",
      sessionId.value,
      "micStream:",
      !!micStream.value,
    );

    if (!sessionId.value || !micStream.value) {
      console.warn(
        "[startAnalysis] 조건 불충족으로 종료. sessionId:",
        sessionId.value,
        "micStream:",
        !!micStream.value,
      );
      return;
    }
    console.log(
      "[startAnalysis] 호출됨. sessionId:",
      sessionId.value,
      "micStream:",
      !!micStream.value,
    );

    if (!sessionId.value || !micStream.value) {
      console.warn(
        "[startAnalysis] 조건 불충족으로 종료. sessionId:",
        sessionId.value,
        "micStream:",
        !!micStream.value,
      );
      return;
    }

    try {
      console.log("[startAnalysis] 세션 분석 시작:", sessionId.value);
      // [Modified] 분석 시작 후 서버 시간을 받아와서 타임라인 기준으로 사용
      const res = await videoApi.post(
        `/review/start/analysis/${sessionId.value}`,
      );
      console.log("[startAnalysis] 분석 시작됨. 서버 응답:", res.data);
      console.log("[startAnalysis] 세션 분석 시작:", sessionId.value);
      // [Modified] 분석 시작 후 서버 시간을 받아와서 타임라인 기준으로 사용
      const res = await videoApi.post(
        `/review/start/analysis/${sessionId.value}`,
      );
      console.log("[startAnalysis] 분석 시작됨. 서버 응답:", res.data);
      isAnalysisStarted.value = true;

      // startTime 파싱 및 시간 동기화 (학생과 동일한 방식)
      if (res.data.startTime) {
        const serverTime = new Date(res.data.startTime);
        const clientTime = new Date();

        // 서버 시작 시간 저장
        classStartTime.value = serverTime.getTime();

        // ✅ 클라이언트-서버 시간 차이 계산 (학생과 동일)
        timeOffset.value = clientTime.getTime() - serverTime.getTime();

        console.log(
          `[startAnalysis] 수업 시작 시간 설정: ${res.data.startTime} (${classStartTime.value})`,
        );
        console.log(
          `[startAnalysis] 시간 오프셋: ${timeOffset.value}ms (클라이언트-서버 차이)`,
        );

        // ✅ 학생들에게 수업 시작 시간 브로드캐스트
        if (stompClient && sessionId.value) {
          console.log(
            `[startAnalysis] Broadcasting start time to students: ${res.data.startTime}`,
          );
          stompClient.send(
            `/app/session/${sessionId.value}/start-time`,
            {},
            JSON.stringify({ startTime: res.data.startTime }),
          );
        }
      } else {
        classStartTime.value = Date.now(); // Fallback
      }

      // startTime 파싱 및 시간 동기화 (학생과 동일한 방식)
      if (res.data.startTime) {
        const serverTime = new Date(res.data.startTime);
        const clientTime = new Date();

        // 서버 시작 시간 저장
        classStartTime.value = serverTime.getTime();

        // ✅ 클라이언트-서버 시간 차이 계산 (학생과 동일)
        timeOffset.value = clientTime.getTime() - serverTime.getTime();

        console.log(
          `[startAnalysis] 수업 시작 시간 설정: ${res.data.startTime} (${classStartTime.value})`,
        );
        console.log(
          `[startAnalysis] 시간 오프셋: ${timeOffset.value}ms (클라이언트-서버 차이)`,
        );

        // ✅ 학생들에게 수업 시작 시간 브로드캐스트
        if (stompClient && sessionId.value) {
          console.log(
            `[startAnalysis] Broadcasting start time to students: ${res.data.startTime}`,
          );
          stompClient.send(
            `/app/session/${sessionId.value}/start-time`,
            {},
            JSON.stringify({ startTime: res.data.startTime }),
          );
        }
      } else {
        classStartTime.value = Date.now(); // Fallback
      }

      startVoiceAnalysis(micStream.value);
      console.log("[startAnalysis] 음성 분석 시작 완료");
      console.log("[startAnalysis] 음성 분석 시작 완료");
    } catch (e) {
      console.error("[startAnalysis] 분석 시작 API 오류", e);
      console.error("[startAnalysis] 분석 시작 API 오류", e);
      alert("분석 시작에 실패했습니다.");
    }
  }
  /**
   * 5. 화면 공유 소스 변경
   * - 수업 중 화면 공유를 다른 화면으로 변경
   * - WebRTC 트랙만 교체 (세션 유지)
   */
  async function changeScreenShare() {
    if (!roomHandle.value) return;

    try {
      // 1. 새 화면 공유 스트림 획득
      const newStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true,
      });
      const newVideoTrack = newStream.getVideoTracks()[0];
      if (!newVideoTrack) {
        throw new Error("새 스트림에서 비디오 트랙을 찾을 수 없습니다");
        throw new Error("새 스트림에서 비디오 트랙을 찾을 수 없습니다");
      }

      // 2. PeerConnection에서 Video Sender 찾기
      // @ts-ignore (webrtcStuff는 janus.js 내부 객체)
      const pc = roomHandle.value.webrtcStuff.pc;
      if (pc) {
        const senders = pc.getSenders();
        const videoSender = senders.find(
          (s: any) => s.track && s.track.kind === "video",
        );

        if (videoSender) {
          // 기존 트랙의 종료 핸들러 제거 (세션 종료 방지)
          if (videoSender.track) {
            videoSender.track.onended = null;
            videoSender.track.stop(); // 기존 트랙 중지
          }

          // 3. 트랙 교체
          await videoSender.replaceTrack(newVideoTrack);
          console.log("화면 공유 트랙 교체 성공");
          console.log("화면 공유 트랙 교체 성공");

          // 로컬 미리보기 업데이트
          screenStream.value = newStream;

          // 새 트랙에 종료 핸들러 부착
          newVideoTrack.onended = () => stopClass();
        }
      }
    } catch (e) {
      console.error("화면 공유 변경 오류", e);
      console.error("화면 공유 변경 오류", e);
    }
  }

  // 분석결과 + 채팅/퀴즈 수신용 stomp 연결 (통합)
  let stompClient: any = null;

  const handleConnectWs = () => {
    // ✅ 올바른 환경 변수 사용 (프로토콜 포함)
    const socket = new SockJS(import.meta.env.VITE_CHAT_SOCKET_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const token = localStorage.getItem("accessToken");

    stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
      console.log("🌐 STOMP 소켓 연결 성공");
      handleSubscribe(); // 연결 성공하면 자동으로 구독
    });
  };

  const handleSubscribe = () => {
    stompClient.subscribe(
      `/topic/session/${sessionId.value}/participants`,
      (res: any) => {
        console.log("👥 [Participants Event]:", res.body);
        try {
          const data = JSON.parse(res.body);
          console.log("[Participants] Parsed data:", data);

          if (data.type === "JOIN") {
            // ✅ STOMP JOIN 이벤트로 Participant를 미리 생성 (정확한 memberId 확보)
            console.log(
              "👉 Student JOIN (STOMP):",
              data.name,
              "memberId:",
              data.memberId,
              "feedId:",
              data.feedId,
            );

            if (data.role === "STUDENT" && data.feedId) {
              addParticipantFromStomp(data);
            }
          } else if (data.type === "LEAVE") {
            console.log("👈 Student LEAVE:", data.name, "feedId:", data.feedId);
            if (data.feedId) {
              removeStudent(data.feedId);
            }
          }
        } catch (e) {
          console.error("Participants Event Parse Error", e);
        }
      },
    );

    // 2. 채팅/질문 구독
    stompClient.subscribe(`/topic/chat-room/${sessionId.value}`, (res: any) => {
      console.log(`[Chat/QA Message]:`, res.body);
      const msg = JSON.parse(res.body);
      if (!msg.questionId) {
        chatList.value.push(msg);
      } else {
        qaList.value.push(msg);
      }
    });

    // 3. 퀴즈 구독
    stompClient.subscribe(`/topic/quiz/${sessionId.value}`, (res: any) => {
      console.log(`[Quiz 수신]:`, res.body);
      const parsedData = JSON.parse(res.body);
      console.log(`[Quiz 파싱됨]:`, parsedData);
      quizData.value = parsedData;
      console.log(`[Quiz quizData 업데이트 완료]:`, quizData.value);
    });
    console.log(`🔔 모든 구독 완료 (sessionId: ${sessionId.value})`);
  };

  // 메시지 전송 함수
  const sendChatMessage = (
    content: string,
    type: string,
    questionId?: number | string | null,
  ) => {
    if (!content.trim() || !stompClient?.connected) return;
    const payload = {
      type,
      content,
      questionId: questionId || null,
    };
    stompClient.send(
      `/app/chat-room/${sessionId.value}`,
      {},
      JSON.stringify(payload),
    );
  };

  const sendQuizToStudents = (quizzes: any) => {
    if (!stompClient?.connected) return;
    stompClient.send(
      `/app/quiz/send/${sessionId.value}`,
      {},
      JSON.stringify(quizzes),
    );
    console.log("[Store] Quiz sent to students:", quizzes);
  };

  const getQuiz = () => {
    console.log(
      "[getQuiz] 호출됨. stompClient:",
      stompClient,
      "connected:",
      stompClient?.connected,
    );
    if (!stompClient?.connected) {
      console.warn("[getQuiz] STOMP 연결 안됨!");
      return;
    }
    const payload = { content: "퀴즈주세요" };
    console.log(
      "[getQuiz] 메시지 전송:",
      payload,
      "to:",
      `/app/quiz/${sessionId.value}/test`,
    );
    stompClient.send(
      `/app/quiz/${sessionId.value}/test`, // ✅ 서버로 전송 (백엔드 @MessageMapping)
      {},
      JSON.stringify(payload),
    );
    console.log("[getQuiz] 메시지 전송 완료");
  };

  // =================================================================================
  /**
   * 6. 음성 분석 시작
   * - WebSocket으로 음성 데이터를 백엔드에 전송
   * - 10초마다 녹음을 끊어서 전송 (EBML 헤더 문제 해결)
   */
  function startVoiceAnalysis(stream: MediaStream) {
    if (!sessionId.value) return;

    try {
      // WebSocket URL 구성
      const protocol =
        window.location.protocol === "https:" ? "wss://" : "ws://";

      let wsHost = window.location.hostname; // 기본값
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

      // API URL에서 호스트 추출
      if (apiBaseUrl) {
        try {
          const urlObj = new URL(apiBaseUrl);
          wsHost = urlObj.host;
        } catch (e) {
          console.warn("Invalid API URL, using default");
        }
      }

      const wsUrl = `${protocol}${wsHost}/voice/${sessionId.value}`;
      console.log("Connecting Voice WS:", wsUrl);

      const ws = new WebSocket(wsUrl);
      voiceSocket.value = ws;

      ws.onopen = () => {
        console.log("Voice WS Connected");

        /**
         * 10초마다 녹음을 끊어서 전송하는 재귀 함수
         * - MediaRecorder로 10초간 녹음
         * - Blob으로 변환 후 WebSocket으로 전송
         * - 타임라인 정보 포함 (4바이트 헤더)
         */
        const recordAndSend = () => {
          if (!isAnalysisStarted.value || ws.readyState !== WebSocket.OPEN)
            return;

          // ✅ 녹음 시작 시점의 timeline 계산 (서버 시간 기준, 학생과 동일한 방식)
          const now = Date.now();
          const serverNow = now - timeOffset.value; // 클라이언트 시간 → 서버 시간
          const recordingStartTime = Math.floor(
            (serverNow - classStartTime.value) / 1000,
          );

          // ✅ 녹음 시작 시점의 timeline 계산 (서버 시간 기준, 학생과 동일한 방식)
          const now = Date.now();
          const serverNow = now - timeOffset.value; // 클라이언트 시간 → 서버 시간
          const recordingStartTime = Math.floor(
            (serverNow - classStartTime.value) / 1000,
          );

          // MediaRecorder 생성
          const recorder = new MediaRecorder(stream, {
            mimeType: "audio/webm",
            bitsPerSecond: 75000,
          });
          mediaRecorder.value = recorder;

          const chunks: BlobPart[] = []; // 녹음 데이터 저장

          // 데이터 수집
          recorder.ondataavailable = (e) => {
            if (e.data.size > 0) chunks.push(e.data);
          };

          // 녹음 종료 시 전송
          recorder.onstop = async () => {
            const blob = new Blob(chunks, { type: "audio/webm" });
            if (blob.size > 0) {
              // ✅ 녹음 시작 시점의 timeline 사용 (비동기 지연 보정)
              const timeline = Math.max(0, recordingStartTime);
              // ✅ 녹음 시작 시점의 timeline 사용 (비동기 지연 보정)
              const timeline = Math.max(0, recordingStartTime);

              // 바이너리 데이터 구성 (4바이트 헤더 + 오디오 데이터)
              const audioBuffer = await blob.arrayBuffer();
              const payload = new Uint8Array(4 + audioBuffer.byteLength);
              const view = new DataView(payload.buffer);

              // 첫 4바이트: 타임라인 (Int32, Big Endian)
              view.setInt32(0, timeline, false);
              view.setInt32(0, timeline, false);
              // 나머지: 오디오 데이터
              payload.set(new Uint8Array(audioBuffer), 4);

              // WebSocket으로 전송
              if (ws.readyState === WebSocket.OPEN) {
                ws.send(payload);
                console.log(
                  `Sent Voice Chunk (Header Included): ${timeline}s, ${blob.size} bytes`,
                );
              }
            }

            // 다음 10초 녹음 시작 (재귀 호출)
            if (isAnalysisStarted.value) recordAndSend();
          };

          // 녹음 시작
          recorder.start();

          // 10초 후 자동 종료
          setTimeout(() => {
            if (recorder.state === "recording") recorder.stop();
          }, 10000);
        };

        recordAndSend(); // 첫 녹음 시작
      };

      ws.onerror = (e) => console.error("Voice WS Error", e);
      ws.onclose = () => console.log("Voice WS Closed");
    } catch (e) {
      console.error("Voice Analysis Start Failed", e);
    }
  }

  /**
   * 7. 수업 종료
   * - 백엔드에 분석 종료 알림
   * - Janus 방 폭파
   * - 미디어 스트림 정리
   * - 음성 분석 중지
   */
  async function stopClass() {
    if (!roomHandle.value) return;

    try {
      // 타임라인 계산 (수업 마지막 시간)
      const now = Date.now();
      const lastTimeline = Math.ceil((now - classStartTime.value) / 1000);

      // 백엔드에 분석 종료 요청
      await videoApi.post(`/review/end/analysis/${sessionId.value}`, {
        pauseElementList: pauseHistory.value, // 실제 쉬는 시간 이력 전송
        teacherId: userStore.user.memberId, // 강사 ID
        lastTimeline: lastTimeline > 0 ? lastTimeline : 1,
        videoUrl: "asda", // 녹화 URL (실제 구현 시 업데이트 필요)
      });
      console.log("분석 종료됨");
      console.log("분석 종료됨");

      // 세션 종료 및 Janus 방 폭파
    } catch (e) {
      console.error("수업 종료 API 오류", e);
      console.error("수업 종료 API 오류", e);
    }
    await videoApi.post(`/session/${sessionId.value}/stop`);
    console.log("세션 중지됨 (Janus 방 파괴)");
    console.log("세션 중지됨 (Janus 방 파괴)");

    // Janus unpublish
    const unpublish = { request: "unpublish" };
    roomHandle.value.send({ message: unpublish });
    isAnalysisStarted.value = false;
    isSessionStarted.value = false;
    if (screenStream.value) {
      screenStream.value.getTracks().forEach((t) => t.stop());
      screenStream.value = null;
    }

    // 음성 분석 중지
    if (mediaRecorder.value) {
      if (mediaRecorder.value.state !== "inactive") mediaRecorder.value.stop();
      mediaRecorder.value = null;
    }
    if (voiceSocket.value) {
      voiceSocket.value.close();
      voiceSocket.value = null;
    }
    if (micStream.value) {
      micStream.value.getTracks().forEach((t) => t.stop());
      micStream.value = null;
    }
    classStartTime.value = 0;

    // 쉬는 시간 상태 초기화
    isOnBreak.value = false;
    currentPauseStart.value = null;
    pauseHistory.value = [];
  }

  /**
   * 8. 쉬는 시간 토글 (시작/종료)
   * - 쉬는 시간 버튼 클릭 시 호출
   */
  function toggleBreak() {
    if (!classStartTime.value) return; // 수업 시작 전이면 무시

    if (!isOnBreak.value) {
      // 쉬는 시간 시작
      isOnBreak.value = true;
      currentPauseStart.value = Date.now();
      console.log("⏸ Break Started at", currentPauseStart.value);
    } else {
      // 쉬는 시간 종료
      if (currentPauseStart.value) {
        const now = Date.now();
        // 타임라인 기준 초 단위 변환
        const startSeconds = Math.floor(
          (currentPauseStart.value - classStartTime.value) / 1000,
        );
        const endSeconds = Math.ceil((now - classStartTime.value) / 1000);

        pauseHistory.value.push({
          startTime: startSeconds > 0 ? startSeconds : 0,
          endTime: endSeconds > startSeconds ? endSeconds : startSeconds + 1,
        });
        console.log("▶ Break Ended. History:", pauseHistory.value);
      }
      isOnBreak.value = false;
      currentPauseStart.value = null;
    }

    // 쉬는 시간 상태 초기화
    isOnBreak.value = false;
    currentPauseStart.value = null;
    pauseHistory.value = [];
  }

  /**
   * 8. 쉬는 시간 토글 (시작/종료)
   * - 쉬는 시간 버튼 클릭 시 호출
   */
  function toggleBreak() {
    if (!classStartTime.value) return; // 수업 시작 전이면 무시

    if (!isOnBreak.value) {
      // 쉬는 시간 시작
      isOnBreak.value = true;
      currentPauseStart.value = Date.now();
      console.log("⏸ Break Started at", currentPauseStart.value);
    } else {
      // 쉬는 시간 종료
      if (currentPauseStart.value) {
        const now = Date.now();
        // 타임라인 기준 초 단위 변환
        const startSeconds = Math.floor(
          (currentPauseStart.value - classStartTime.value) / 1000,
        );
        const endSeconds = Math.ceil((now - classStartTime.value) / 1000);

        pauseHistory.value.push({
          startTime: startSeconds > 0 ? startSeconds : 0,
          endTime: endSeconds > startSeconds ? endSeconds : startSeconds + 1,
        });
        console.log("▶ Break Ended. History:", pauseHistory.value);
      }
      isOnBreak.value = false;
      currentPauseStart.value = null;
    }
  }

  // ========================================
  // 학생 관리
  // ========================================

  /**
   * 입장한 학생들 처리
   * - publisher 목록에서 학생만 필터링 (선생 제외)
   * - 각 학생에 대해 구독 시작
   */
  function handleIncomingStudents(publishers: Publisher[]) {
    console.log(
      "[handleIncomingStudents] Received publishers:",
      publishers.length,
    );
    publishers.forEach((p, i) => {
      console.log(`[handleIncomingStudents] Publisher[${i}]:`, {
        id: p.id,
        display: p.display,
        isTeacher: p.display?.includes("[선생]"),
      });
    console.log(
      "[handleIncomingStudents] Received publishers:",
      publishers.length,
    );
    publishers.forEach((p, i) => {
      console.log(`[handleIncomingStudents] Publisher[${i}]:`, {
        id: p.id,
        display: p.display,
        isTeacher: p.display?.includes("[선생]"),
      });
      if (p.display && !p.display.includes("[선생]")) {
        console.log(
          `[handleIncomingStudents] → Subscribing to student feedId: ${p.id}`,
        );
        console.log(
          `[handleIncomingStudents] → Subscribing to student feedId: ${p.id}`,
        );
        subscribeToStudent(p);
      }
    });
  }

  /**
   * 학생 스트림 구독
   * - 새로운 플러그인 핸들 생성 (subscriber용)
   * - Janus에 구독 요청
   * - participants 목록에 추가
   */
  function subscribeToStudent(publisher: Publisher) {
    const feedId = publisher.id; // 학생의 Feed ID
    console.log(
      "[subscribeToStudent] feedId:",
      feedId,
      "publisher:",
      publisher,
    );

    if (remoteFeeds.value.has(feedId)) {
      console.warn("[subscribeToStudent] 이미 구독 중:", feedId);
      return; // 이미 구독 중
    }
    console.log(
      "[subscribeToStudent] feedId:",
      feedId,
      "publisher:",
      publisher,
    );

    if (remoteFeeds.value.has(feedId)) {
      console.warn("[subscribeToStudent] 이미 구독 중:", feedId);
      return; // 이미 구독 중
    }

    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: JanusHandle) => {
        console.log("[subscribeToStudent] Handle attached for feedId:", feedId);
        console.log("[subscribeToStudent] Handle attached for feedId:", feedId);
        remoteFeeds.value.set(feedId, handle); // 핸들 저장

        // 구독 요청
        const subscribe = {
          request: "join",
          room: roomInfo.value.janusRoomId,
          ptype: "subscriber", // 타입: 구독자
          streams: [{ feed: feedId }], // 구독할 Feed ID
          pin: roomInfo.value.pin,
        };
        handle.send({ message: subscribe });

        // Display 이름 파싱
        // 형식: "memberId|[학생] 이름"
        let displayStr = publisher.display || `학생 ${feedId}`;
        console.log(
          `[subscribeToStudent] 🔍 Parsing display name for feed ${feedId}: "${displayStr}"`,
        );

        let pMemberId = String(feedId); // Fallback
        let pName = displayStr;

        if (displayStr.includes("|")) {
          const parts = displayStr.split("|");
          if (parts.length >= 2) {
            pMemberId = parts[0]!; // 실제 회원 ID
            pName = parts[1]!; // "[학생] 이름"
            console.log(
              `[subscribeToStudent] ✅ Parsed memberId: ${pMemberId}, name: ${pName}`,
            );
          }
        } else {
          console.warn(
            `[subscribeToStudent] ⚠️ Display name does not contain '|'. Using feedId as memberId.`,
          );
        }

        const pIdStr = String(feedId); // Feed ID를 문자열로 사용

        // ✅ 이미 존재하는지 확인
        const existingParticipant = participants.value.find(
          (p) => p.id === pIdStr,
        );

        if (existingParticipant) {
          console.log(
            `[subscribeToStudent] ♻️ Participant already exists. Updating stream info...`,
          );
          // 이미 존재하면 정보 업데이트 (혹시 display 파싱으로 더 정확한 정보가 있다면)
          if (pMemberId !== String(feedId))
            existingParticipant.memberId = pMemberId;
          // 스트림 연결 로직은 아래에서 계속 진행됨 (Janus attach)
        } else {
          // 존재하지 않으면 신규 추가 (Janus만으로 입장한 경우)
          console.log(
            "[subscribeToStudent] ✅ Adding NEW participant (Janus-only):",
            {
              feedId: pIdStr,
              memberId: pMemberId,
              name: pName,
              originalDisplay: publisher.display,
            },
          );

          // participants 목록에 추가
          participants.value.push({
            id: pIdStr, // ⚠️ feedId를 id로 사용!
            memberId: pMemberId, // 실제 회원 ID
            name: pName,
            avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${pMemberId}`, // 아바타 (회원 ID 기반)
            isCameraOn: true,
            isMicOn: true,
            concentrationScore: 50, // 초기 집중도 (백엔드에서 업데이트 예정)
            status: "NORMAL", // 초기 상태
            isVideoStreamReady: false, // ✅ 초기값: 스트림 준비 안됨
          });
        }

        console.log(
          "[subscribeToStudent] 📋 All participants:",
          participants.value.map((p) => ({
            id: p.id,
            memberId: p.memberId,
            name: p.name,
          })),
        );

        console.log(
          "[subscribeToStudent] participants updated:",
          participants.value.length,
          "students",
        );
      },
      onmessage: (msg: JanusMessage, jsep: JSEP) => {
        console.log(`[Subscriber] onmessage - feedId: ${feedId}`, msg, jsep);

        console.log(`[Subscriber] onmessage - feedId: ${feedId}`, msg, jsep);

        // SDP Answer 처리
        if (jsep) {
          console.log(
            `[Subscriber] Handling JSEP (Offer) for feedId: ${feedId}`,
          );
          console.log(
            `[Subscriber] Handling JSEP (Offer) for feedId: ${feedId}`,
          );
          const handle = remoteFeeds.value.get(feedId);
          handle?.createAnswer({
            jsep,
            media: { audioSend: false, videoSend: false }, // 수신만 함
            success: (jsepAnswer: JSEP) => {
              console.log(
                `[Subscriber] createAnswer success for feedId: ${feedId}`,
              );
              console.log(
                `[Subscriber] createAnswer success for feedId: ${feedId}`,
              );
              const start = {
                request: "start",
                room: roomInfo.value.janusRoomId,
              };
              handle.send({ message: start, jsep: jsepAnswer });
            },
            error: (e: any) =>
              console.error(
                `[Subscriber] createAnswer Error for feedId: ${feedId}`,
                e,
              ),
            error: (e: any) =>
              console.error(
                `[Subscriber] createAnswer Error for feedId: ${feedId}`,
                e,
              ),
          });
        }
      },
      // 원격 트랙 수신 (학생의 비디오/오디오)
      onremotetrack: (track: MediaStreamTrack, mid: string, on: boolean) => {
        console.log(
          "[onremotetrack] feedId:",
          feedId,
          "track:",
          track.kind,
          "on:",
          on,
        );
        console.log(
          "[onremotetrack] feedId:",
          feedId,
          "track:",
          track.kind,
          "on:",
          on,
        );
        if (!on) return;

        const pIdStr = String(feedId);
        let stream = studentStreams.value.get(pIdStr);

        // 스트림이 없으면 생성
        if (!stream) {
          console.log(
            "[onremotetrack] 🎥 Creating new stream for feedId:",
            pIdStr,
          );
          stream = new MediaStream();
          studentStreams.value.set(pIdStr, stream);
        }

        stream.addTrack(track); // 트랙 추가
        console.log(
          "[onremotetrack] ✅ Track added. Total streams:",
          studentStreams.value.size,
        );
        console.log(
          "[onremotetrack] 🔑 studentStreams keys (feedIds):",
          Array.from(studentStreams.value.keys()),
        );

        // ✅ 비디오 트랙을 받으면 스트림 준비 완료로 표시
        if (track.kind === "video") {
          const participant = participants.value.find((p) => p.id === pIdStr);
          if (participant) {
            participant.isVideoStreamReady = true;
            console.log(
              "[onremotetrack] 📹 Video stream ready for participant:",
              participant.name,
            );
          }
        }
      },
      // ❌ oncleanup 제거: 무한 루프 원인!
      // Janus leaving/unpublished 이벤트와 STOMP LEAVE로 충분함
    });
  }

  /**
   * 학생 제거
   * - 핸들 해제
   * - participants 목록에서 제거
   * - 스트림 삭제
   */
  function removeStudent(feedId: number) {
    console.log("[removeStudent] feedId:", feedId);

    const pIdStr = String(feedId);

    // ✅ 이미 제거된 경우 스킵 (무한 루프 방지)
    if (!remoteFeeds.value.has(feedId)) {
      console.warn("[removeStudent] Already removed:", feedId);
      return;
    }

    console.log("[removeStudent] feedId:", feedId);

    const pIdStr = String(feedId);

    // ✅ 이미 제거된 경우 스킵 (무한 루프 방지)
    if (!remoteFeeds.value.has(feedId)) {
      console.warn("[removeStudent] Already removed:", feedId);
      return;
    }

    const handle = remoteFeeds.value.get(feedId);

    // ✅ 먼저 Map에서 제거 (oncleanup 재호출 방지)
    remoteFeeds.value.delete(feedId);

    // ✅ 그 다음 detach (이제 oncleanup이 호출되어도 early return됨)

    // ✅ 먼저 Map에서 제거 (oncleanup 재호출 방지)
    remoteFeeds.value.delete(feedId);

    // ✅ 그 다음 detach (이제 oncleanup이 호출되어도 early return됨)
    if (handle) {
      try {
        handle.detach();
      } catch (e) {
        console.error("[removeStudent] Detach error:", e);
      }
      try {
        handle.detach();
      } catch (e) {
        console.error("[removeStudent] Detach error:", e);
      }
    }

    // 스트림 및 참여자 목록에서 제거
    // 스트림 및 참여자 목록에서 제거
    studentStreams.value.delete(pIdStr);
    participants.value = participants.value.filter((p) => p.id !== pIdStr);

    // PIP에 표시 중이면 제거
    if (selectedStudentId.value === pIdStr) {
      selectedStudentId.value = null;
    }

    console.log(
      "[removeStudent] Removed. Remaining participants:",
      participants.value.length,
    );
  }

  /**
   * STOMP JOIN 메시지로 학생 추가
   * - memberId와 name의 진실(Truth)은 여기서 온다
   */
  function addParticipantFromStomp(data: any) {
    const memberIdStr = String(data.memberId);
    const feedIdStr = String(data.feedId);

    // 이미 존재하는지 확인 (id = feedId 기준)
    const existing = participants.value.find((p) => p.id === feedIdStr);

    if (existing) {
      console.log(
        `[STOMP] ♻️ Participant already exists: ${data.name} (memberId: ${existing.memberId} -> ${memberIdStr})`,
      );
      existing.memberId = memberIdStr; // memberId 업데이트 (가장 중요)
      existing.name = data.name;
      return;
    }

    console.log(
      `[STOMP] ✅ Adding new participant: ${data.name} (memberId: ${memberIdStr}, feedId: ${feedIdStr})`,
    );

    participants.value.push({
      id: feedIdStr, // Janus Feed ID (매칭 키)
      memberId: memberIdStr, // 정확한 Member ID
      name: data.name,
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${memberIdStr}`,
      isCameraOn: true,
      isMicOn: true,
      concentrationScore: 50,
      status: "NORMAL",
      isVideoStreamReady: false,
    });
  }

  /**
   * PIP에 표시할 학생 선택
   */
  function selectStudent(participantId: string) {
    selectedStudentId.value = participantId;
  }

  /**
   * 학생 상태 업데이트
   * - stateChangeList에서 상태 변경 정보를 받아 participants 업데이트
   */
  function updateParticipantStatus(
    memberId: number | string,
    newStatus: "NORMAL" | "FEVER" | "EMPTY",
  ) {
    const memberIdStr = String(memberId);
    console.log(
      `[updateParticipantStatus] 🔍 Looking for memberId: "${memberIdStr}" (type: ${typeof memberIdStr})`,
    );
    console.log(
      `[updateParticipantStatus] 📋 Current participants:`,
      participants.value.map((p) => ({
        id: p.id,
        memberId: p.memberId,
        memberIdType: typeof p.memberId,
        name: p.name,
      })),
    );

    const participant = participants.value.find(
      (p) => p.memberId === memberIdStr,
    );
    if (participant) {
      console.log(
        `[Status] ✅ Updating ${participant.name}: ${participant.status} → ${newStatus}`,
      );
      participant.status = newStatus;
    } else {
      console.warn(
        `[Status] ❌ Participant not found for memberId: ${memberIdStr}`,
      );
    }
  }

  // ========================================
  // Export: 외부에서 사용할 상태 및 메서드
  // ========================================
  return {
    janus,
    initJanus,
    connStatus,
    isSessionStarted,
    isAnalysisStarted,
    startSession,
    startAnalysis,
    stopClass,
    changeScreenShare,
    screenStream,
    roomInfo,
    participants,
    studentStreams,
    selectedStudentId,
    selectStudent,
    updateParticipantStatus,
    updateParticipantStatus,
    chatList,
    qaList,
    quizData,
    sendChatMessage,
    sendQuizToStudents,
    getQuiz,
    // 쉬는 시간
    isOnBreak,
    pauseHistory,
    toggleBreak,
    openSession,
    closeSession,
    quizData,
    sendChatMessage,
    sendQuizToStudents,
    getQuiz,
    // 쉬는 시간
    isOnBreak,
    pauseHistory,
    toggleBreak,
    openSession,
    closeSession,
  };
});

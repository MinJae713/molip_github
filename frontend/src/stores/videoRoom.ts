import { ref, computed, toRaw } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import adapter from "webrtc-adapter";
import JanusPkg from "janus-gateway";
import { useWorkspaceStore } from "@/stores/workspace";

import type { VideoRoom } from "@/types";

// Janus 전역 설정 (브라우저 환경 대응)
if (typeof window !== "undefined") {
  (window as any).adapter = adapter;
}
const Janus = JanusPkg?.Janus || (JanusPkg as any);

// 화상회의 전용 API 클라이언트
const videoApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 5000,
});

// 토큰 주입 인터셉터
videoApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- Types ---

interface JSEP {
  type: string;
  sdp: string;
}

interface JanusMessage {
  videoroom?: string;
  room?: number;
  description?: string;
  id?: number;
  private_id?: number;
  publishers?: Publisher[];
  leaving?: number;
  unpublished?: number;
  error?: string;
  [key: string]: any;
}

interface Publisher {
  id: number;
  display?: string;
  audio_codec?: string;
  video_codec?: string;
  talking?: boolean;
}

interface JanusHandle {
  plugin: string;
  id: number;
  send(params: {
    message: any;
    jsep?: JSEP;
    success?: (data?: any) => void;
    error?: (err: any) => void;
  }): void;
  createOffer(params: {
    stream?: MediaStream | null;
    tracks?: Array<{ type: string; capture: any; recv?: boolean }>;
    media?: {
      audioRecv?: boolean;
      videoRecv?: boolean;
      audioSend?: boolean;
      videoSend?: boolean;
      audio?: any;
      video?: any;
    };
    success: (jsep: JSEP) => void;
    error: (err: any) => void;
  }): void;
  createAnswer(params: {
    jsep: JSEP;
    tracks?: Array<{ type: string; capture: boolean; recv: boolean }>;
    media?: { audioSend?: boolean; videoSend?: boolean };
    success: (jsep: JSEP) => void;
    error?: (err: any) => void;
  }): void;
  handleRemoteJsep(params: { jsep: JSEP }): void;
  detach(params?: { success?: () => void; error?: (err: any) => void }): void;
}

export const useVideoRoomStore = defineStore("videoRoom", () => {
  const spaceStore = useWorkspaceStore();

  // --- State ---
  const janus = ref<any>(null); // Janus instance
  const camHandle = ref<JanusHandle | null>(null);
  const screenHandle = ref<JanusHandle | null>(null); // Keep screen handle

  const roomInfo = ref<VideoRoom | null>(null);
  const connStatus = ref("Ready");
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);

  const myId = ref<number | null>(null);
  const myPrivateId = ref<number | null>(null);
  const feeds = ref<Record<number, JanusHandle>>({});

  // Device State
  const audioDevices = ref<MediaDeviceInfo[]>([]);
  const videoDevices = ref<MediaDeviceInfo[]>([]);
  const selectedAudioDeviceId = ref<string>("");
  const selectedVideoDeviceId = ref<string>("");

  // Streams (for UI)
  const mainStream = ref<MediaStream | null>(null);
  const pipStream = ref<MediaStream | null>(null);
  const localStream = ref<MediaStream | null>(null);

  // Role & Permissions
  // [Modified] User Role Logic restored from original code
  const userRole = computed(() => spaceStore.currentWorkspace?.myRole);

  const publishingCam = ref(false);
  const publishingScreen = ref(false); // Restore screen sharing state

  // --- Actions ---

  /**
   * 디바이스 목록 가져오기
   */
  async function getDevices() {
    try {
      // 권한 요청 (목록 갱신용)
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true,
      });
      stream.getTracks().forEach((track) => track.stop());

      const devices = await navigator.mediaDevices.enumerateDevices();
      audioDevices.value = devices.filter((d) => d.kind === "audioinput");
      videoDevices.value = devices.filter((d) => d.kind === "videoinput");

      // 기본값 설정
      if (!selectedAudioDeviceId.value && audioDevices.value.length > 0) {
        const firstAudio = audioDevices.value[0];
        if (firstAudio) selectedAudioDeviceId.value = firstAudio.deviceId;
      }
      if (!selectedVideoDeviceId.value && videoDevices.value.length > 0) {
        const firstVideo = videoDevices.value[0];
        if (firstVideo) selectedVideoDeviceId.value = firstVideo.deviceId;
      }
      return true;
    } catch (err) {
      console.error("디바이스 목록 조회 실패:", err);
      return false;
    }
  }

  /**
   * 로컬 미리보기 시작
   */
  async function startLocalPreview() {
    try {
      if (localStream.value) {
        localStream.value.getTracks().forEach((t) => t.stop());
      }

      const constraints: MediaStreamConstraints = {
        audio: selectedAudioDeviceId.value
          ? { deviceId: { exact: selectedAudioDeviceId.value } }
          : true,
        video: selectedVideoDeviceId.value
          ? { deviceId: { exact: selectedVideoDeviceId.value } }
          : true,
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      localStream.value = stream;

      return stream;
    } catch (err) {
      console.error("로컬 미리보기 시작 실패:", err);
      throw err;
    }
  }

  /**
   * 디바이스 변경 핸들러
   */
  async function setAudioDevice(deviceId: string) {
    selectedAudioDeviceId.value = deviceId;
    if (localStream.value) await startLocalPreview();
  }

  async function setVideoDevice(deviceId: string) {
    selectedVideoDeviceId.value = deviceId;
    if (localStream.value) await startLocalPreview();
  }

  function setAudioEnabled(enabled: boolean) {
    if (localStream.value) {
      localStream.value
        .getAudioTracks()
        .forEach((track) => (track.enabled = enabled));
    }
  }

  function setVideoEnabled(enabled: boolean) {
    if (localStream.value) {
      localStream.value
        .getVideoTracks()
        .forEach((track) => (track.enabled = enabled));
    }
  }

  /**
   * 방 정보 조회 (Session ID 기반)
   */
  async function fetchRoomInfo(sessionId: string | number) {
    if (!userRole.value) {
      console.warn("User userRole is not defined. Workspace join required.");
    }
    connStatus.value = "방 정보 확인 중...";
    try {
      const res = await videoApi.get(`/session/${sessionId}/connection`);

      if (res.data) {
        roomInfo.value = {
          janusRoomId: res.data.janusRoomId,
          pin: res.data.pin,
          roomAlive: true,
        };
        console.log("방 정보 획득:", roomInfo.value);
        return true;
      }
      return false;
    } catch (err: any) {
      console.error("방 정보 조회 실패:", err);
      if (err.response && err.response.status === 404) {
        connStatus.value = "방이 존재하지 않습니다.";
      } else {
        connStatus.value = "방 정보를 불러올 수 없습니다.";
      }
      roomInfo.value = null;
      return false;
    }
  }

  /**
   * 방 생성하기
   */
  async function createRoom(sessionId: string | number) {
    try {
      connStatus.value = "방 생성 중...";
      const res = await videoApi.post(`/session/${sessionId}/start`);
      roomInfo.value = {
        janusRoomId: res.data.janusRoomId,
        pin: res.data.pin,
        roomAlive: true,
      };
      console.log("방 생성 성공!");
      return true;
    } catch (err: any) {
      console.error("방 생성 실패", err);
      connStatus.value = "방 생성에 실패했습니다.";
      return false;
    }
  }

  /**
   * Janus 초기화 및 연결
   */
  async function initJanus(sessionId: string | number) {
    const roomOk = await fetchRoomInfo(sessionId);
    if (!roomOk) {
      throw new Error("방 정보를 찾을 수 없습니다.");
    }

    return new Promise((resolve, reject) => {
      Janus.init({
        debug: "all",
        callback: () => {
          const protocol =
            window.location.protocol === "https:" ? "wss://" : "ws://";
          const token = localStorage.getItem("accessToken");
          const serverUrl =
            import.meta.env.VITE_JANUS_URL ||
            `${protocol}${window.location.hostname}/janus_ws`;

          let finalServer = serverUrl;
          const params = [];
          if (token) params.push(`auth=${token}`);
          if (sessionId) params.push(`session=${sessionId}`);

          if (params.length > 0) {
            finalServer += `?${params.join("&")}`;
          }

          janus.value = new Janus({
            server: finalServer,
            iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
            success: async () => {
              connStatus.value = "Connected";
              console.log("[Janus] 서버 연결 성공");

              try {
                // Camera 플러그인 부착 및 자동 입장
                await attachPlugin("camera");
                if (camHandle.value) joinRoom(camHandle.value, "camera");
                resolve(true);
              } catch (e) {
                console.error("Plugin attach/join failed", e);
                reject(e);
              }
            },
            error: (err: any) => {
              connStatus.value = "Connection Error";
              reject(err);
            },
            destroyed: () => {
              console.log("[Janus] 세션 파괴됨");
              window.location.reload();
            },
          });
        },
      });
    });
  }

  /**
   * VideoRoom 플러그인 부착
   */
  async function attachPlugin(type: "camera" | "screen") {
    if (!janus.value) return;

    return new Promise((resolve) => {
      janus.value.attach({
        plugin: "janus.plugin.videoroom",
        opaqueId: opaqueId.value,
        success: (handle: JanusHandle) => {
          if (type === "camera") camHandle.value = handle;
          else screenHandle.value = handle;

          console.log(`[${type}] 플러그인 부착 성공`);
          resolve(handle);
        },
        error: (err: any) => console.error(`[${type}] 부착 오류`, err),
        onmessage: (msg: JanusMessage, jsep: JSEP) => {
          const handle =
            type === "camera" ? camHandle.value : screenHandle.value;
          if (handle) handleMessage(handle, type, msg, jsep);
        },
        onlocaltrack: (track: MediaStreamTrack, on: boolean) => {
          handleLocalTrack(type, track, on);
        },
        oncleanup: () => console.log(`[${type}] Cleanup`),
      });
    });
  }

  /**
   * 방 입장 (Join as Publisher)
   */
  function joinRoom(handle: JanusHandle, type: "camera" | "screen") {
    if (!roomInfo.value) return;

    // Display Name Generation
    const displayRole = userRole.value === "TRAINER" ? "teacher" : "student";
    // Construct unique display name: role_type (e.g., teacher_camera, student_screen)
    const displayName = `${displayRole}_${type}`;

    const register = {
      request: "join",
      room: roomInfo.value.janusRoomId,
      ptype: "publisher",
      display: displayName,
      pin: roomInfo.value.pin,
    };
    handle.send({ message: register });
  }

  /**
   * 퍼블리셔 목록 처리 (필터링 및 구독)
   */
  function handleIncomingPublishers(publishers: Publisher[]) {
    // 학생 필터: [선생] 포함된 퍼블리셔만 구독 -> teacher_ prefix 체크로 변경
    if (userRole.value === "LEARNER" || userRole.value === "student") {
      publishers = publishers.filter(
        (p) => p.display && p.display.startsWith("teacher_"),
      );
    }
    publishers.forEach((p) => processPublisher(p));
  }

  /**
   * 메시지 처리 핸들러
   */
  function handleMessage(
    handle: JanusHandle,
    type: string,
    msg: JanusMessage,
    jsep: JSEP,
  ) {
    const event = msg["videoroom"];
    if (event === "joined") {
      myId.value = msg["id"] || null;
      myPrivateId.value = msg["private_id"] || null;
      console.log(`[${type}] 방 입장 성공. ID: ${myId.value}`);

      // [Modified] TRAINER always publishes camera on join? Or maybe just manual?
      // Keeping original behavior: Student publishes camera automatically.
      if (
        (userRole.value === "LEARNER" || userRole.value === "student") &&
        type === "camera"
      ) {
        publishCamera();
      }

      if (msg["publishers"]) {
        handleIncomingPublishers(msg["publishers"]);
      }
    } else if (event === "event") {
      if (msg["publishers"]) {
        handleIncomingPublishers(msg["publishers"]);
      } else if (msg["leaving"] || msg["unpublished"]) {
        const id = msg["leaving"] || msg["unpublished"];
        if (id && feeds.value[id]) {
          feeds.value[id].detach();
          delete feeds.value[id];
        }
      }
    }

    if (jsep) {
      console.log("Handling Remote JSEP (Answer/Offer):", jsep);
      handle.handleRemoteJsep({ jsep });
    }
  }

  /**
   * 카메라 스트림 송출
   */
  async function publishCamera() {
    if (!camHandle.value || publishingCam.value) return;
    publishingCam.value = true;

    // [Modified] Configure media based on role
    // Trainer: Send Audio/Video
    // Learner: Send Audio/Video? (Assuming interactive class)
    // Original code said: audioSend: userRole.value === "TRAINER"
    // Let's stick to safe defaults.
    const isTrainer = userRole.value === "TRAINER";

    const mediaOpts: any = {
      audioRecv: false,
      videoRecv: false,
      audioSend: true, // Everyone sends audio? Or conditional? Let's Default True for now.
      videoSend: true,
      keepAudio: true,
      keepVideo: true,
    };

    if (selectedAudioDeviceId.value)
      mediaOpts.audio = { deviceId: { exact: selectedAudioDeviceId.value } };
    if (selectedVideoDeviceId.value)
      mediaOpts.video = { deviceId: { exact: selectedVideoDeviceId.value } };

    const rawStream = toRaw(localStream.value);

    // Check tracks before creating offer
    const audioTrack = rawStream?.getAudioTracks()[0];
    const videoTrack = rawStream?.getVideoTracks()[0];

    camHandle.value.createOffer({
      tracks: [
        {
          type: "audio",
          capture: audioTrack ? audioTrack.clone() : null,
          recv: false,
        },
        {
          type: "video",
          capture: videoTrack ? videoTrack.clone() : null,
          recv: false,
        },
      ],
      success: (jsep: JSEP) => {
        console.log("Generated Publisher JSEP (Offer):", jsep);
        const publish = {
          request: "publish",
          record: isTrainer, // Only record trainer?
        };
        if (camHandle.value) {
          camHandle.value.send({ message: publish, jsep });
        }
      },
      error: (err: any) => {
        console.error("Camera Offer Error", err);
        publishingCam.value = false;
      },
    });
  }

  /**
   * 화면 공유 송출 (Screen) - [Restored]
   */
  async function publishScreen() {
    // Ensure plugin attached
    if (!screenHandle.value) {
      await attachPlugin("screen");
      if (screenHandle.value) joinRoom(screenHandle.value, "screen");
    }

    if (!screenHandle.value || publishingScreen.value) return;
    publishingScreen.value = true;

    screenHandle.value.createOffer({
      media: {
        video: "screen", // This triggers getDisplayMedia in Janus.js
        audioRecv: false,
        videoRecv: false,
        audioSend: true, // System audio?
        videoSend: true,
      },
      success: (jsep: JSEP) => {
        const publish = { request: "publish", record: true };
        screenHandle.value?.send({ message: publish, jsep });
      },
      error: (err: any) => {
        console.error("Screen Offer Error", err);
        publishingScreen.value = false;
      },
    });
  }

  /**
   * 로컬 트랙 로직
   */
  function handleLocalTrack(
    type: string,
    track: MediaStreamTrack,
    on: boolean,
  ) {
    if (!on) return;
    const stream = new MediaStream([track]);
    if (type === "screen") {
      // If we are sharing screen, maybe show it in main component?
      // Or just keep it separate.
    } else {
      localStream.value = stream;
    }
  }

  /**
   * 퍼블리셔 구독 처리
   */
  function processPublisher(publisher: Publisher) {
    if (feeds.value[publisher.id]) return;
    if (publisher.display) {
      subscribeTo(publisher.id, publisher.display);
    }
  }

  /**
   * UI 테스트용 Mock Stream
   */
  function simulateRemoteStream(type: "main" | "pip") {
    const canvas = document.createElement("canvas");
    canvas.width = 640;
    canvas.height = 360;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const colors = ["#ff0000", "#00ff00", "#0000ff", "#ffff00", "#00ffff"];
    let colorIndex = 0;

    setInterval(() => {
      const color = colors[colorIndex];
      if (color) ctx.fillStyle = color;
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.fillStyle = "white";
      ctx.font = "48px serif";
      ctx.fillText(type === "main" ? "Teacher Screen" : "Teacher Cam", 50, 100);
      ctx.fillText(new Date().toLocaleTimeString(), 50, 200);
      colorIndex = (colorIndex + 1) % colors.length;
    }, 1000);

    const stream = canvas.captureStream(30);
    if (type === "main") mainStream.value = stream;
    else pipStream.value = stream;
  }

  /**
   * 원격 피드 구독 (Subscriber)
   */
  function subscribeTo(feedId: number, display: string) {
    let remoteFeed: JanusHandle | null = null;

    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: JanusHandle) => {
        remoteFeed = handle;
        feeds.value[feedId] = handle;

        const body = {
          request: "join",
          room: roomInfo.value?.janusRoomId,
          ptype: "subscriber",
          streams: [{ feed: feedId }],
          private_id: myPrivateId.value,
          pin: roomInfo.value?.pin,
        };
        handle.send({ message: body });
      },
      onmessage: (msg: JanusMessage, jsep: JSEP) => {
        if (jsep) {
          if (remoteFeed) {
            remoteFeed.createAnswer({
              jsep,
              tracks: [
                { type: "audio", capture: false, recv: true },
                { type: "video", capture: false, recv: true },
              ],
              success: (jsepAnswer: JSEP) => {
                const body = {
                  request: "start",
                  room: roomInfo.value?.janusRoomId,
                };
                if (remoteFeed) {
                  remoteFeed.send({ message: body, jsep: jsepAnswer });
                }
              },
            });
          } else {
            console.error("remoteFeed not ready for jsep", feedId);
          }
        }
      },
      onremotetrack: (track: MediaStreamTrack, mid: string, on: boolean) => {
        if (!on) return;
        const stream = new MediaStream([track]);

        // teacher_screen -> Main
        // teacher_camera -> PIP
        if (display && display.startsWith("teacher_")) {
          if (display.includes("screen")) mainStream.value = stream;
          else if (display.includes("camera")) pipStream.value = stream;
          else {
            // Backward compatibility or default fallback
            pipStream.value = stream;
          }
        }
      },
    });
  }

  return {
    janus,
    roomInfo,
    connStatus,
    mainStream,
    pipStream,
    localStream,
    userRole,
    camHandle,
    screenHandle,
    // Device Actions
    audioDevices,
    videoDevices,
    selectedAudioDeviceId,
    selectedVideoDeviceId,
    fetchRoomInfo,
    createRoom,
    initJanus,
    attachPlugin,
    joinRoom,
    publishCamera,
    publishScreen,
    getDevices,
    startLocalPreview,
    setAudioDevice,
    setVideoDevice,
    setAudioEnabled,
    setVideoEnabled,
    simulateRemoteStream,
  };
});

import { ref, computed } from "vue";
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

// 화상회의 전용 API 클라이언트 (8080 포트 직접 연결)
const videoApi = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 5000,
});

// 토큰 주입 인터셉터
videoApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("auth");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const useVideoRoomStore = defineStore("videoRoom", () => {
  const spaceStore = useWorkspaceStore();

  // --- 상태 (State) ---
  const janus = ref<any>(null);
  const camHandle = ref<any>(null);
  const screenHandle = ref<any>(null);
  const roomInfo = ref<VideoRoom | null>(null);
  const connStatus = ref("Ready");
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);

  const myId = ref<number | null>(null);
  const myPrivateId = ref<number | null>(null);
  const feeds = ref<Record<number, any>>({});

  // 디바이스 관리 (State)
  const audioDevices = ref<MediaDeviceInfo[]>([]);
  const videoDevices = ref<MediaDeviceInfo[]>([]);
  const selectedAudioDeviceId = ref<string>("");
  const selectedVideoDeviceId = ref<string>("");

  // 스트림 (UI 바인딩 용)
  const mainStream = ref<MediaStream | null>(null);
  const pipStream = ref<MediaStream | null>(null);
  const localStream = ref<MediaStream | null>(null);

  // 역할 및 권한 (Workspace Store에서 가져옴)
  const userRole = computed(() => spaceStore.currentWorkspace?.myRole);
  const publishingCam = ref(false);
  const publishingScreen = ref(false);

  // --- Actions ---

  /**
   * 0. 디바이스 목록 가져오기
   */
  async function getDevices() {
    try {
      // 권한 요청을 위해 한 번 스트림을 열었다가 닫음 (브라우저 정책 대응)
      // 이미 권한이 있다면 생략 가능하지만, 확실한 목록 갱신을 위해 수행
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
   * 0-1. 로컬 미리보기 (Preview) 시작
   */
  async function startLocalPreview() {
    try {
      if (localStream.value) {
        localStream.value.getTracks().forEach((t) => t.stop());
      }

      const constraints = {
        audio: selectedAudioDeviceId.value
          ? { deviceId: { exact: selectedAudioDeviceId.value } }
          : true,
        video: selectedVideoDeviceId.value
          ? { deviceId: { exact: selectedVideoDeviceId.value } }
          : true,
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      localStream.value = stream;

      // 트랙 기본값은 Enabled (미리보기 화면이 나와야 함)
      // 초기에는 사용자가 볼 수 있어야 하므로 강제로 끄지 않음.
      // 필요하다면 별도의 isVideoMuted 상태를 통해 제어해야 함.

      return stream;
    } catch (err) {
      console.error("로컬 미리보기 시작 실패:", err);
      throw err;
    }
  }

  /**
   * 0-2. 디바이스 변경
   */
  async function setAudioDevice(deviceId: string) {
    selectedAudioDeviceId.value = deviceId;
    // 스트림이 이미 있다면 교체
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
   * 1. 방 정보 가져오기 (단순 확인)
   */
  async function fetchRoomInfo(courseId: string | number) {
    if (!userRole.value) {
      console.warn("User userRole is not defined. Workspace join required.");
    }
    connStatus.value = "방 정보 확인 중...";
    try {
      const res = await videoApi.get(`/rooms/courses/${courseId}`);
      roomInfo.value = {
        janusRoomId: res.data.janusRoomId,
        pin: res.data.pin,
        roomAlive: true,
      };
      return true;
    } catch (err: any) {
      if (err.response && err.response.status === 404) {
        console.log("방이 존재하지 않습니다.");
        roomInfo.value = null;
        return false;
      } else {
        connStatus.value = "방 정보를 불러올 수 없습니다.";
        throw err;
      }
    }
  }

  /**
   * 1-2. 방 생성하기 (명시적 호출 필요)
   */
  async function createRoom(courseId: string | number) {
    try {
      connStatus.value = "방 생성 중...";
      // Use dedicated videoApi client
      const res = await videoApi.post(`/rooms/courses/${courseId}`);
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
   * 2. Janus 라이브러리 초기화 및 연결
   */
  async function initJanus() {
    return new Promise((resolve, reject) => {
      Janus.init({
        debug: "all",
        callback: () => {
          const protocol =
            window.location.protocol === "https:" ? "wss://" : "ws://";
          const server = `${protocol}${window.location.hostname}/janus_ws`;

          janus.value = new Janus({
            server,
            iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
            success: () => {
              connStatus.value = "Connected";
              console.log("[Janus] 서버 연결 성공");
              resolve(true);
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
   * 3. 비디오룸 플러그인 부착
   */
  async function attachPlugin(type: "camera" | "screen") {
    if (!janus.value) return;

    return new Promise((resolve) => {
      janus.value.attach({
        plugin: "janus.plugin.videoroom",
        opaqueId: opaqueId.value,
        success: (handle: any) => {
          if (type === "camera") camHandle.value = handle;
          else screenHandle.value = handle;
          console.log(`[${type}] 플러그인 부착 성공`);
          resolve(handle);
        },
        error: (err: any) => console.error(`[${type}] 부착 오류`, err),
        onmessage: (msg: any, jsep: any) => {
          const handle =
            type === "camera" ? camHandle.value : screenHandle.value;
          handleMessage(handle, type, msg, jsep);
        },
        onlocaltrack: (track: MediaStreamTrack, on: boolean) => {
          handleLocalTrack(type, track, on);
        },
        oncleanup: () => console.log(`[${type}] Cleanup`),
      });
    });
  }

  /**
   * 4. 방 입장 (Join)
   */
  function joinRoom(handle: any, type: "camera" | "screen") {
    if (!roomInfo.value) return;
    const register = {
      request: "join",
      room: roomInfo.value.janusRoomId,
      ptype: "publisher",
      display: `${userRole.value}_${type}`,
      pin: roomInfo.value.pin,
    };
    handle.send({ message: register });
  }

  /**
   * 5. 메시지 핸들링 (샘플 로직 통합)
   */
  function handleMessage(handle: any, type: string, msg: any, jsep: any) {
    const event = msg["videoroom"];
    if (event === "joined") {
      myId.value = msg["id"];
      myPrivateId.value = msg["private_id"];
      console.log(`[${type}] 방 입장 성공. ID: ${myId.value}`);

      if (userRole.value === "LEARNER" && type === "camera") {
        publishCamera(); // 학생은 자동 카메라 송출
      }

      if (msg["publishers"]) {
        let publishers = msg["publishers"];
        if (userRole.value === "student") {
          publishers = publishers.filter((p: any) =>
            p.display?.startsWith("teacher_"),
          );
        }
        publishers.forEach((p: any) => processPublisher(p));
      }
    } else if (event === "event") {
      if (msg["publishers"]) {
        let publishers = msg["publishers"];
        if (userRole.value === "student") {
          publishers = publishers.filter((p: any) =>
            p.display?.startsWith("teacher_"),
          );
        }
        publishers.forEach((p: any) => processPublisher(p));
      } else if (msg["leaving"] || msg["unpublished"]) {
        const id = msg["leaving"] || msg["unpublished"];
        if (feeds.value[id]) {
          feeds.value[id].detach();
          delete feeds.value[id];
        }
      }
    }

    if (jsep) {
      handle.handleRemoteJsep({ jsep });
    }
  }

  /**
   * 6. 스트림 송출 (Camera)
   */
  async function publishCamera() {
    if (!camHandle.value || publishingCam.value) return;
    publishingCam.value = true;

    const media: any = {
      audioRecv: false,
      videoRecv: false,
      audioSend: userRole.value === "TRAINER",
      videoSend: true,
    };

    // 만약 로컬 프리뷰 스트림이 있다면 그대로 사용 (권한 재요청 방지)
    if (localStream.value) {
      // Janus createOffer의 'stream' 옵션 사용
      // 주의: stream 옵션을 쓰면 media의 capture 관련 설정은 무시될 수 있음
    } else {
      // 스트림이 없으면 디바이스 ID 적용
      if (selectedAudioDeviceId.value)
        media.audio = { deviceId: { exact: selectedAudioDeviceId.value } };
      if (selectedVideoDeviceId.value)
        media.video = { deviceId: { exact: selectedVideoDeviceId.value } };
    }

    camHandle.value.createOffer({
      stream: localStream.value, // stream이 null이면 내부에서 getUserMedia 호출 (media 옵션 기반)
      media: localStream.value ? undefined : media,
      success: (jsep: any) => {
        const publish = {
          request: "publish",
          record: userRole.value === "TRAINER",
        };
        camHandle.value.send({ message: publish, jsep });
      },
      error: (err: any) => {
        console.error("Camera Offer Error", err);
        publishingCam.value = false;
      },
    });
  }

  /**
   * 7. 화면 공유 송출 (Screen)
   */
  async function publishScreen() {
    if (!screenHandle.value || publishingScreen.value) return;
    publishingScreen.value = true;

    screenHandle.value.createOffer({
      media: {
        video: "screen",
        audioRecv: false,
        videoRecv: false,
        audioSend: true,
        videoSend: true,
      },
      success: (jsep: any) => {
        const publish = { request: "publish", record: true };
        screenHandle.value.send({ message: publish, jsep });
      },
      error: (err: any) => {
        console.error("Screen Offer Error", err);
        publishingScreen.value = false;
      },
    });
  }

  /**
   * 로컬 트랙 처리
   */
  function handleLocalTrack(
    type: string,
    track: MediaStreamTrack,
    on: boolean,
  ) {
    if (!on) return;
    const stream = new MediaStream([track]);
    if (type === "screen") {
      mainStream.value = stream;
    } else {
      localStream.value = stream;
    }
  }

  /**
   * 퍼블리셔 감지 및 구독
   */
  function processPublisher(publisher: any) {
    if (feeds.value[publisher.id]) return;
    subscribeTo(publisher.id, publisher.display);
  }

  /**
   * UI 테스트용 Mock Stream 생성
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
   * 원격 피드 구독
   */
  function subscribeTo(feedId: number, display: string) {
    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: any) => {
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
      onmessage: (msg: any, jsep: any) => {
        if (jsep) {
          feeds.value[feedId].createAnswer({
            jsep,
            tracks: [
              { type: "audio", capture: false, recv: true },
              { type: "video", capture: false, recv: true },
            ],
            success: (jsepAnswer: any) => {
              const body = {
                request: "start",
                room: roomInfo.value?.janusRoomId,
              };
              feeds.value[feedId].send({ message: body, jsep: jsepAnswer });
            },
          });
        }
      },
      onremotetrack: (track: MediaStreamTrack, mid: string, on: boolean) => {
        if (!on) return;
        const stream = new MediaStream([track]);
        if (display.startsWith("TRAINER_")) {
          if (display.includes("screen")) mainStream.value = stream;
          else pipStream.value = stream;
        }
        // TODO: 학생 그리드 처리는 UI 요구사항에 맞게 추가 확장 가능
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
    fetchRoomInfo,
    createRoom,
    initJanus,
    attachPlugin,
    joinRoom,
    publishCamera,
    publishScreen,
    camHandle,
    screenHandle,
    // Device Actions
    audioDevices,
    videoDevices,
    selectedAudioDeviceId,
    selectedVideoDeviceId,
    getDevices,
    startLocalPreview,
    setAudioDevice,
    setVideoDevice,
    setAudioEnabled,
    setVideoEnabled,
    simulateRemoteStream,
  };
});

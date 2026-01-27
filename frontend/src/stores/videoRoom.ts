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

  // 스트림 (UI 바인딩 용)
  const mainStream = ref<MediaStream | null>(null);
  const pipStream = ref<MediaStream | null>(null);
  const localStream = ref<MediaStream | null>(null);

  // 역할 및 권한 (Workspace Store에서 가져옴)
  const userRole = computed(() => spaceStore.currentWorkspace?.my_role);
  const publishingCam = ref(false);
  const publishingScreen = ref(false);

  // --- Actions ---

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
        janus_room_id: res.data.janusRoomId,
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
        janus_room_id: res.data.janusRoomId,
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
      room: roomInfo.value.janus_room_id,
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

    camHandle.value.createOffer({
      media: {
        audioRecv: false,
        videoRecv: false,
        audioSend: userRole.value === "TRAINER",
        videoSend: true,
      },
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
          room: roomInfo.value?.janus_room_id,
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
                room: roomInfo.value?.janus_room_id,
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
  };
});

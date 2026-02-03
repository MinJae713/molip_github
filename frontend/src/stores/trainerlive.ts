import { ref, computed, toRaw } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import adapter from "webrtc-adapter";
import JanusPkg from "janus-gateway";
import { useWorkspaceStore } from "@/stores/workspace";
import { useUserStore } from "@/stores/user";
import type { Participant } from "@/types/live";

// Janus 전역 설정
if (typeof window !== "undefined") {
  (window as any).adapter = adapter;
}
const Janus = JanusPkg?.Janus || (JanusPkg as any);

// 화상회의 API 클라이언트
const videoApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 5000,
});

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
  publishers?: Publisher[];
  leaving?: number;
  unpublished?: number;
  error?: string;
  [key: string]: any;
}

export interface Publisher {
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
    tracks?: Array<{ type: string; capture: any; recv?: boolean }>;
    media?: {
      audioRecv?: boolean;
      videoRecv?: boolean;
      audioSend?: boolean;
      videoSend?: boolean;
    };
    success: (jsep: JSEP) => void;
    error: (err: any) => void;
  }): void;
  createAnswer(params: any): void;
  handleRemoteJsep(params: { jsep: JSEP }): void;
  detach(params?: any): void;
}

export const useTrainerLiveStore = defineStore("trainerLive", () => {
  const spaceStore = useWorkspaceStore();
  const userStore = useUserStore();

  // --- State ---
  const janus = ref<any>(null);
  const roomHandle = ref<JanusHandle | null>(null); // 본인(강사) 핸들

  const roomInfo = ref<any>(null);
  const connStatus = ref("Ready");
  const opaqueId = ref(`trainer-${Janus.randomString(12)}`);

  const myId = ref<number | null>(null);
  const sessionId = ref<string | number>("");
  const isAnalysisStarted = ref(false); // 수업/분석 시작 여부

  // Streams
  const screenStream = ref<MediaStream | null>(null); // 강사 화면 공유 스트림
  const micStream = ref<MediaStream | null>(null); // 강사 마이크 스트림 (음성 분석용)

  // Voice Analysis
  const voiceSocket = ref<WebSocket | null>(null);
  const mediaRecorder = ref<MediaRecorder | null>(null);
  const classStartTime = ref<number>(0); // 수업 시작 시간 (타임라인 기준점)

  // Student Management
  const participants = ref<Participant[]>([]); // 입장한 학생 목록 (UI용)
  const remoteFeeds = ref<Map<number, JanusHandle>>(new Map()); // 학생 구독 핸들 (feedId -> Handle)
  const studentStreams = ref<Map<string, MediaStream>>(new Map()); // 학생 비디오 스트림 (participantId(string) -> Stream)

  const selectedStudentId = ref<string | null>(null); // PIP에 띄울 학생 ID (string)

  // --- Actions ---

  async function initJanus(id: string | number) {
    sessionId.value = 7;
    try {
      const res = await videoApi.get(`/session/${id}/connection`);
      roomInfo.value = {
        janusRoomId: res.data.janusRoomId,
        pin: res.data.pin,
      };
    } catch (e) {
      console.error("방 정보 조회 실패", e);
      connStatus.value = "Session Info Error";
      return;
    }

    Janus.init({
      debug: "all",
      callback: () => {
        const protocol =
          window.location.protocol === "https:" ? "wss://" : "ws://";
        const envUrl = import.meta.env.VITE_JANUS_URL;
        const serverUrl =
          envUrl || `${protocol}${window.location.hostname}/janus_ws`;

        const token = localStorage.getItem("accessToken");
        let finalServer = `${serverUrl}?session=${id}`;
        if (token) {
          finalServer += `&auth=${token}`;
        }

        janus.value = new Janus({
          server: finalServer,
          iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
          success: async () => {
            connStatus.value = "Connected";
            attachPlugin();
          },
          error: (err: any) => {
            console.error("Janus Connect Error", err);
            connStatus.value = "Connection Error";
          },
        });
      },
    });
  }

  function attachPlugin() {
    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: JanusHandle) => {
        roomHandle.value = handle;
        joinRoom();
      },
      error: (err: any) => console.error("Plugin Attach Error", err),
      onmessage: (msg: JanusMessage, jsep: JSEP) => onMessage(msg, jsep),
      onlocaltrack: (track: MediaStreamTrack, on: boolean) => {
        if (on && track.kind === "video") {
          screenStream.value = new MediaStream([track]);
        }
      },
      oncleanup: () => {
        // screenStream.value = null; // stopClass에서 처리
      },
    });
  }

  function joinRoom() {
    if (!roomHandle.value || !roomInfo.value) return;
    const displayName = `[선생] 강사`;
    const register = {
      request: "join",
      room: roomInfo.value.janusRoomId,
      ptype: "publisher",
      display: displayName,
      pin: roomInfo.value.pin,
    };
    roomHandle.value.send({ message: register });
  }

  function onMessage(msg: JanusMessage, jsep: JSEP) {
    const event = msg["videoroom"];

    if (event === "joined") {
      myId.value = msg["id"];
      if (msg["publishers"]) handleIncomingStudents(msg["publishers"]);
    } else if (event === "event") {
      if (msg["publishers"]) handleIncomingStudents(msg["publishers"]);
      else if (msg["leaving"] || msg["unpublished"]) {
        const id = msg["leaving"] || msg["unpublished"];
        if (id) removeStudent(id);
      }
    }

    if (jsep && roomHandle.value) {
      roomHandle.value.handleRemoteJsep({ jsep });
    }
  }

  /**
   * 4. 수업 시작 (화면 공유 시작 + 음성 분석 시작)
   */
  async function startClass() {
    if (isAnalysisStarted.value) return;

    try {
      // 1. 화면 공유 스트림
      const displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true,
      });
      const videoTrack = displayStream.getVideoTracks()[0];
      if (!videoTrack) {
        throw new Error("No video track found");
      }

      // 2. 마이크 스트림 (강사 음성 분석 및 송출용)
      const userMicStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
      });
      const audioTrack = userMicStream.getAudioTracks()[0];

      micStream.value = userMicStream;

      if (!roomHandle.value) return;

      // 3. Janus Publish (Video: Screen, Audio: Mic)
      roomHandle.value.createOffer({
        tracks: [
          { type: "video", capture: videoTrack, recv: false },
          { type: "audio", capture: audioTrack, recv: false },
        ],
        success: async (jsep: JSEP) => {
          const publish = { request: "configure", audio: true, video: true };
          roomHandle.value!.send({ message: publish, jsep });

          // [New] 분석 시작 API 호출
          try {
            await videoApi.post(`/review/start/analysis/${sessionId.value}`);
            console.log("Analysis Started");
          } catch (e) {
            console.error("Start Analysis API Error", e);
          }

          isAnalysisStarted.value = true;
          classStartTime.value = Date.now(); // 타임라인 기준점 설정

          // 4. 음성 분석 시작 (마이크 스트림 원본 사용)
          startVoiceAnalysis(userMicStream);
        },
        error: (err: any) => console.error("Screen Share Offer Error", err),
      });

      // 화면 공유 중지 시 수업 종료
      videoTrack.onended = () => stopClass();
    } catch (e) {
      console.error("Device Permission Error", e);
      alert("화면 공유 및 마이크 권한이 필요합니다.");
    }
  }

  /**
   * 5. 화면 공유 소스 변경 (세션 유지)
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
        throw new Error("No video track found in new stream");
      }

      // 2. Janus PeerConnection의 Sender 찾아서 트랙 교체
      // @ts-ignore (webrtcStuff는 janus.js 내부 객체)
      const pc = roomHandle.value.webrtcStuff.pc;
      if (pc) {
        const senders = pc.getSenders();
        const videoSender = senders.find(
          (s: any) => s.track && s.track.kind === "video",
        );

        if (videoSender) {
          // 기존 트랙 onended 제거 (세션 종료 방지)
          if (videoSender.track) {
            videoSender.track.onended = null;
            videoSender.track.stop();
          }

          await videoSender.replaceTrack(newVideoTrack);
          console.log("Screen Share Track Replaced Successfully");

          // 로컬 미리보기 상태 업데이트
          screenStream.value = newStream;

          // 새 트랙에 종료 핸들러 부착
          newVideoTrack.onended = () => stopClass();
        }
      }
    } catch (e) {
      console.error("Change Screen Share Error", e);
    }
  }

  /**
   * 음성 분석용 웹소켓 연결 및 전송
   */
  function startVoiceAnalysis(stream: MediaStream) {
    if (!sessionId.value) return;

    try {
      const protocol =
        window.location.protocol === "https:" ? "wss://" : "ws://";

      let wsHost = window.location.hostname; // Fallback default
      let apiBase = "i14a309.p.ssafy.io";

      // 프로토콜이 없으면 https:// 붙여서 파싱 시도
      if (apiBase && !apiBase.startsWith("http")) {
        apiBase = `https://${apiBase}`;
      }

      if (apiBase) {
        try {
          const urlObj = new URL(apiBase);
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

        // EBML 헤더 문제를 해결하기 위해 10초마다 녹음을 끊어서(start/stop) 새로 생성합니다.
        const recordAndSend = () => {
          if (!isAnalysisStarted.value || ws.readyState !== WebSocket.OPEN)
            return;

          const recorder = new MediaRecorder(stream, {
            mimeType: "audio/webm",
            bitsPerSecond: 75000,
          });
          mediaRecorder.value = recorder;

          const chunks: BlobPart[] = [];

          recorder.ondataavailable = (e) => {
            if (e.data.size > 0) chunks.push(e.data);
          };

          recorder.onstop = async () => {
            const blob = new Blob(chunks, { type: "audio/webm" });
            if (blob.size > 0) {
              // 10초간 녹음했으므로, 타임라인은 (현재 - 시작)
              const timeline = Math.ceil(
                (Date.now() - classStartTime.value) / 1000,
              );

              const audioBuffer = await blob.arrayBuffer();
              const payload = new Uint8Array(4 + audioBuffer.byteLength);
              const view = new DataView(payload.buffer);

              view.setInt32(0, timeline > 0 ? timeline : 0, false);
              payload.set(new Uint8Array(audioBuffer), 4);

              if (ws.readyState === WebSocket.OPEN) {
                ws.send(payload);
                console.log(
                  `Sent Voice Chunk (Header Included): ${timeline}s, ${blob.size} bytes`,
                );
              }
            }
            // 재귀 호출로 다음 10초 녹음 시작
            if (isAnalysisStarted.value) recordAndSend();
          };

          recorder.start();
          setTimeout(() => {
            if (recorder.state === "recording") recorder.stop();
          }, 10000);
        };

        recordAndSend();
      };

      ws.onerror = (e) => console.error("Voice WS Error", e);
      ws.onclose = () => console.log("Voice WS Closed");
    } catch (e) {
      console.error("Voice Analysis Start Failed", e);
    }
  }

  async function stopClass() {
    if (!roomHandle.value) return;

    try {
      const now = Date.now();
      const lastTimeline = Math.ceil((now - classStartTime.value) / 1000);
      const teacherId = myId.value ? String(myId.value) : "0"; // 현재 Janus Client ID를 강사 ID로 가정 (혹은 Store에서 memberId 가져와야 함)

      // [New] 분석 종료 API 호출 (가장 먼저)
      // request body: { pauseElementList: [], teacherId: ..., lastTimeline: ..., videoUrl: ... }
      // 현재 teacherId나 videoUrl 등은 정확한 값을 알 수 없으므로 임시 값 또는 Store 상태 사용 필요
      // 여기서는 일단 간소하게 호출
      await videoApi.post(`/review/end/analysis/${sessionId.value}`, {
        pauseElementList: [{ startTime: 100, endTime: 120 }],
        teacherId: userStore.user.memberId, // UserStore에서 ID 사용
        lastTimeline: lastTimeline > 0 ? lastTimeline : 1,
        videoUrl: "asda", // 녹화 URL이 있다면 여기에
      });
      console.log("Analysis Ended");

      // [New] 세션 종료 및 Janus 방 폭파 API 호출
      await videoApi.post(`/session/stop/${sessionId.value}`);
      console.log("Session Stopped (Janus Room Destroyed)");
    } catch (e) {
      console.error("Stop Class API Error", e);
    }

    // Janus Unpublish
    const unpublish = { request: "unpublish" };
    roomHandle.value.send({ message: unpublish });
    isAnalysisStarted.value = false;
    screenStream.value = null;

    // Voice Analysis Stop
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
  }

  // --- Student Handling ---

  function handleIncomingStudents(publishers: Publisher[]) {
    publishers.forEach((p) => {
      if (p.display && !p.display.includes("[선생]")) {
        subscribeToStudent(p);
      }
    });
  }

  function subscribeToStudent(publisher: Publisher) {
    const feedId = publisher.id;
    if (remoteFeeds.value.has(feedId)) return;

    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: JanusHandle) => {
        remoteFeeds.value.set(feedId, handle);

        const subscribe = {
          request: "join",
          room: roomInfo.value.janusRoomId,
          ptype: "subscriber",
          streams: [{ feed: feedId }],
          pin: roomInfo.value.pin,
        };
        handle.send({ message: subscribe });

        // Display Name Parsing (Strategy 1: memberId|username)
        let displayStr = publisher.display || `학생 ${feedId}`;
        let pMemberId = String(feedId); // Fallback to feedId if not found
        let pName = displayStr;

        if (displayStr.includes("|")) {
          const parts = displayStr.split("|");
          if (parts.length >= 2) {
            pMemberId = parts[0]!;
            pName = parts[1]!; // The rest is the name (e.g. "[학생] 이름")
          }
        }

        const pIdStr = String(feedId); // Maintain feed ID as primary key for now
        participants.value.push({
          id: pIdStr,
          memberId: pMemberId, // Store real member ID
          name: pName,
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${pMemberId}`, // Use Member ID for consistent avatar
          isCameraOn: true,
          isMicOn: true,
          concentrationScore: 50,
          status: "focus",
        });
      },
      onmessage: (msg: JanusMessage, jsep: JSEP) => {
        if (jsep) {
          const handle = remoteFeeds.value.get(feedId);
          handle?.createAnswer({
            jsep,
            media: { audioSend: false, videoSend: false },
            success: (jsepAnswer: JSEP) => {
              const start = {
                request: "start",
                room: roomInfo.value.janusRoomId,
              };
              handle.send({ message: start, jsep: jsepAnswer });
            },
            error: (e: any) => console.error("Subscribe Answer Error", e),
          });
        }
      },
      onremotetrack: (track: MediaStreamTrack, mid: string, on: boolean) => {
        if (!on) return;
        const pIdStr = String(feedId);
        let stream = studentStreams.value.get(pIdStr);
        if (!stream) {
          stream = new MediaStream();
          studentStreams.value.set(pIdStr, stream);
        }
        stream.addTrack(track);
      },
      oncleanup: () => removeStudent(feedId),
    });
  }

  function removeStudent(feedId: number) {
    const handle = remoteFeeds.value.get(feedId);
    if (handle) {
      handle.detach();
      remoteFeeds.value.delete(feedId);
    }
    const pIdStr = String(feedId);
    studentStreams.value.delete(pIdStr);
    participants.value = participants.value.filter((p) => p.id !== pIdStr);

    if (selectedStudentId.value === pIdStr) {
      selectedStudentId.value = null;
    }
  }

  function selectStudent(participantId: string) {
    selectedStudentId.value = participantId;
  }

  return {
    janus,
    initJanus,
    connStatus,
    isAnalysisStarted,
    startClass,
    stopClass,
    changeScreenShare,
    screenStream,
    roomInfo,
    participants,
    studentStreams,
    selectedStudentId,
    selectStudent,
  };
});

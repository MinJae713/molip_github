import { ref, computed, toRaw } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import adapter from "webrtc-adapter";
import JanusPkg from "janus-gateway";
import { useWorkspaceStore } from "@/stores/workspace";
import { useUserStore } from "@/stores/user";
import type { Participant } from "@/types/live";

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

  // --- 미디어 스트림 ---
  const screenStream = ref<MediaStream | null>(null); // 강사의 화면 공유 스트림 (학생들에게 전송됨)
  const micStream = ref<MediaStream | null>(null); // 강사의 마이크 스트림 (음성 분석용)

  // --- 음성 분석 ---
  const voiceSocket = ref<WebSocket | null>(null); // 음성 분석 서버 WebSocket 연결
  const mediaRecorder = ref<MediaRecorder | null>(null); // 마이크 오디오 녹음기
  const classStartTime = ref<number>(0); // 수업 시작 시간 (타임라인 기준점, timestamp)

  // --- 학생 관리 ---
  const participants = ref<Participant[]>([]); // 입장한 학생 목록 (UI 표시용)
  const remoteFeeds = ref<Map<number, JanusHandle>>(new Map()); // 학생별 구독 핸들 (feedId -> Handle)
  const studentStreams = ref<Map<string, MediaStream>>(new Map()); // 학생별 비디오 스트림 (participantId -> Stream)
  const selectedStudentId = ref<string | null>(null); // PIP(Picture-in-Picture)에 표시할 학생 ID

  // --- 채팅/질문 관리 (WebSocket으로 관리, 여기서는 참조용) ---
  const chatList = ref<any[]>([]); // 채팅 메시지 목록
  const qaList = ref<any[]>([]); // 질문/답변 메시지 목록

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

    // Janus 라이브러리 초기화
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

        // Janus 인스턴스 생성 및 연결
        janus.value = new Janus({
          server: finalServer,
          iceServers: [{ urls: "stun:stun.l.google.com:19302" }], // STUN 서버 (NAT 통과용)
          success: async () => {
            connStatus.value = "Connected";
            attachPlugin(); // 연결 성공 시 VideoRoom 플러그인 부착
          },
          error: (err: any) => {
            console.error("Janus Connect Error", err);
            connStatus.value = "Connection Error";
          },
        });
      },
    });
  }

  /**
   * 2. VideoRoom 플러그인 부착
   * - Janus의 VideoRoom 플러그인에 연결
   * - 강사용 핸들 생성 (publish용)
   */
  function attachPlugin() {
    janus.value.attach({
      plugin: "janus.plugin.videoroom", // VideoRoom 플러그인
      opaqueId: opaqueId.value, // 클라이언트 식별자
      success: (handle: JanusHandle) => {
        roomHandle.value = handle; // 핸들 저장
        joinRoom(); // 방 입장
      },
      error: (err: any) => console.error("Plugin Attach Error", err),
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
      // 이벤트 처리
      if (msg["publishers"]) {
        // 새로운 학생 입장
        handleIncomingStudents(msg["publishers"]);
      } else if (msg["leaving"] || msg["unpublished"]) {
        // 학생 퇴장
        const id = msg["leaving"] || msg["unpublished"];
        if (id) removeStudent(id);
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
  async function startSession() {
    try {
      // 1. 화면 공유 스트림 획득
      const displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true,
      });
      const videoTrack = displayStream.getVideoTracks()[0];
      if (!videoTrack) {
        throw new Error("No video track found");
      }

      // 2. 마이크 스트림 획득 (강사 음성)
      const userMicStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
      });
      const audioTrack = userMicStream.getAudioTracks()[0];
      micStream.value = userMicStream;

      if (!roomHandle.value) return;

      // 3. Janus에 미디어 송출
      roomHandle.value.createOffer({
        tracks: [
          { type: "video", capture: videoTrack, recv: false },
          { type: "audio", capture: audioTrack, recv: false },
        ],
        success: async (jsep: JSEP) => {
          const publish = { request: "configure", audio: true, video: true };
          roomHandle.value!.send({ message: publish, jsep });

          isSessionStarted.value = true;
          classStartTime.value = Date.now();
        },
        error: (err: any) => console.error("Screen Share Offer Error", err),
      });

      // 화면 공유가 사용자에 의해 중지되면 수업 종료
      videoTrack.onended = () => stopClass();
    } catch (e) {
      console.error("Device Permission Error", e);
      alert("화면 공유 및 마이크 권한이 필요합니다.");
    }
  }

  /**
   * 4-1. AI 분석 시작
   * - 백엔드 분석 API 호출 및 음성 데이터 전송 시작
   */
  async function startAnalysis() {
    if (!sessionId.value || !micStream.value) return;

    try {
      console.log("Starting analysis for session", sessionId.value);
      await videoApi.post(`/review/start/analysis/${sessionId.value}`);
      console.log("Analysis Started");

      isAnalysisStarted.value = true;

      // 음성 분석 WebSocket 시작
      startVoiceAnalysis(micStream.value);
    } catch (e) {
      console.error("Start Analysis API Error", e);
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
        throw new Error("No video track found in new stream");
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
          console.log("Screen Share Track Replaced Successfully");

          // 로컬 미리보기 업데이트
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
              // 타임라인 계산 (수업 시작 후 경과 시간, 초 단위)
              const timeline = Math.ceil(
                (Date.now() - classStartTime.value) / 1000,
              );

              // 바이너리 데이터 구성 (4바이트 헤더 + 오디오 데이터)
              const audioBuffer = await blob.arrayBuffer();
              const payload = new Uint8Array(4 + audioBuffer.byteLength);
              const view = new DataView(payload.buffer);

              // 첫 4바이트: 타임라인 (Int32, Big Endian)
              view.setInt32(0, timeline > 0 ? timeline : 0, false);
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
        pauseElementList: [{ startTime: 100, endTime: 120 }], // 임시 데이터
        teacherId: userStore.user.memberId, // 강사 ID
        lastTimeline: lastTimeline > 0 ? lastTimeline : 1,
        videoUrl: "asda", // 녹화 URL (실제 구현 시 업데이트 필요)
      });
      console.log("Analysis Ended");

      // 세션 종료 및 Janus 방 폭파
    } catch (e) {
      console.error("Stop Class API Error", e);
    }
    await videoApi.post(`/session/${sessionId.value}/stop`);
    console.log("Session Stopped (Janus Room Destroyed)");

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
    publishers.forEach((p) => {
      if (p.display && !p.display.includes("[선생]")) {
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
    if (remoteFeeds.value.has(feedId)) return; // 이미 구독 중

    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: JanusHandle) => {
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
        let pMemberId = String(feedId); // Fallback
        let pName = displayStr;

        if (displayStr.includes("|")) {
          const parts = displayStr.split("|");
          if (parts.length >= 2) {
            pMemberId = parts[0]!; // 실제 회원 ID
            pName = parts[1]!; // "[학생] 이름"
          }
        }

        const pIdStr = String(feedId); // Feed ID를 문자열로 사용

        // participants 목록에 추가
        participants.value.push({
          id: pIdStr,
          memberId: pMemberId, // 실제 회원 ID
          name: pName,
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${pMemberId}`, // 아바타 (회원 ID 기반)
          isCameraOn: true,
          isMicOn: true,
          concentrationScore: 50, // 초기 집중도 (백엔드에서 업데이트 예정)
          status: "focus", // 초기 상태
        });
      },
      onmessage: (msg: JanusMessage, jsep: JSEP) => {
        // SDP Answer 처리
        if (jsep) {
          const handle = remoteFeeds.value.get(feedId);
          handle?.createAnswer({
            jsep,
            media: { audioSend: false, videoSend: false }, // 수신만 함
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
      // 원격 트랙 수신 (학생의 비디오/오디오)
      onremotetrack: (track: MediaStreamTrack, mid: string, on: boolean) => {
        if (!on) return;

        const pIdStr = String(feedId);
        let stream = studentStreams.value.get(pIdStr);

        // 스트림이 없으면 생성
        if (!stream) {
          stream = new MediaStream();
          studentStreams.value.set(pIdStr, stream);
        }

        stream.addTrack(track); // 트랙 추가
      },
      oncleanup: () => removeStudent(feedId), // 정리 시 제거
    });
  }

  /**
   * 학생 제거
   * - 핸들 해제
   * - participants 목록에서 제거
   * - 스트림 삭제
   */
  function removeStudent(feedId: number) {
    const handle = remoteFeeds.value.get(feedId);
    if (handle) {
      handle.detach();
      remoteFeeds.value.delete(feedId);
    }

    const pIdStr = String(feedId);
    studentStreams.value.delete(pIdStr);
    participants.value = participants.value.filter((p) => p.id !== pIdStr);

    // PIP에 표시 중이면 제거
    if (selectedStudentId.value === pIdStr) {
      selectedStudentId.value = null;
    }
  }

  /**
   * PIP에 표시할 학생 선택
   */
  function selectStudent(participantId: string) {
    selectedStudentId.value = participantId;
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
    chatList,
    qaList,
  };
});

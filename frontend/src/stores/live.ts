import { defineStore } from "pinia";
import { ref, computed } from "vue";
import Janus from "janus-gateway";
import adapter from "webrtc-adapter";
import api from "@/api";

import { useTrainerCourseStore } from "./trainerCourse";
import { useTrainerSessionStore } from "./trainerSession";
import type { Participant } from "@/types/live";

export const useLiveStore = defineStore("live", () => {
  const courseStore = useTrainerCourseStore();
  const sessionStore = useTrainerSessionStore();

  // --- [1] 접속 정보 (백엔드 응답 데이터 저장) ---
  const janusRoomId = ref<number>(0);
  const pin = ref<string>("");
  const sessionId = ref<number>(0);

  // --- [2] Janus 객체 상태 ---
  const janus = ref<any>(null);
  const sfutest = ref<any>(null); 
  const monitoringHandle = ref<any>(null); 
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);
  const myid = ref<number | null>(null);

  // --- [3] 미디어 및 UI 상태 ---
  const myStream = ref<MediaStream | null>(null);
  const screenStream = ref<MediaStream | null>(null);
  const isCamOn = ref(false);
  const isMicOn = ref(false);
  const isScreenSharing = ref(false);
  
  const isLiveStarted = ref(false);
  const isConnecting = ref(false);
  const error = ref<string | null>(null);

  const monitoringStream = ref<MediaStream | null>(null);
  const monitoringStudentId = ref<number | null>(null);
  const activePublishers = ref<any[]>([]);
  const participants = ref<Participant[]>([]);

  let syncInterval: number | null = null;

  const liveTitle = computed(() => courseStore.currentManagedCourse?.title || "강의 제목 없음");
  const currentSessionTitle = computed(() => sessionStore.currentManagedSession?.title || "세션 정보 없음");

  // --- [4] 초기화 및 연결 로직 ---

  async function initLiveRoom(cId: string | number, sId: string | number) {
    if (isConnecting.value) return;
    try {
      isConnecting.value = true;
      error.value = null;

      // 1. 백엔드 세션 시작 API 호출 (명세서 반영)
      // POST /session/{session_id}/start
      const sessionResponse = await api.post(`/session/${sId}/start`);
      
      // 전달해주신 JSON 구조에 맞게 데이터 할당
      janusRoomId.value = sessionResponse.data.janusRoomId;
      pin.value = sessionResponse.data.pin;
      sessionId.value = sessionResponse.data.sessionId;

      // 2. 기초 데이터 로드
      await Promise.all([
        courseStore.setManagedCourse(cId),
        sessionStore.fetchSessionDetail(Number(sId)),
        fetchParticipants(cId)
      ]);

      // 3. Janus 초기화
      Janus.init({
        debug: "all",
        dependencies: Janus.useDefaultDependencies({ adapter }),
        callback: () => {
          if (!Janus.isWebrtcSupported()) {
            error.value = "WebRTC 미지원 브라우저입니다.";
            return;
          }
          createJanusInstance();
        }
      });
    } catch (e: any) {
      error.value = e.response?.data?.message || "연결 실패 (404/500)";
      isConnecting.value = false;
    }
  }

  function createJanusInstance() {
    const token = localStorage.getItem("accessToken");
    // .env에서 서버 주소 가져오기
    const server = `${import.meta.env.VITE_JANUS_SERVER_URL}?auth=${token}`;

    janus.value = new Janus({
      server: server,
      success: () => {
        janus.value.attach({
          plugin: "janus.plugin.videoroom",
          opaqueId: opaqueId.value,
          success: (handle: any) => {
            sfutest.value = handle;
            joinRoom();
            startPublisherSync();
          },
          onmessage: (msg: any, jsep: any) => {
            handleJanusMessage(msg, jsep);
          },
          onlocalstream: (stream: MediaStream) => {
            myStream.value = stream;
          }
        });
      },
      error: (err: any) => {
        error.value = "서버 연결 실패";
        isConnecting.value = false;
      }
    });
  }

  function handleJanusMessage(msg: any, jsep: any) {
    const event = msg["videoroom"];
    if (event === "joined") {
      myid.value = msg["id"];
      isLiveStarted.value = true;
      isConnecting.value = false;
      if (msg["publishers"]) activePublishers.value = msg["publishers"];
    } else if (event === "event") {
      if (msg["publishers"]) {
        activePublishers.value = [...activePublishers.value, ...msg["publishers"]];
      } else if (msg["leaving"] || msg["unpublished"]) {
        const leftId = Number(msg["leaving"] || msg["unpublished"]);
        if (monitoringStudentId.value === leftId) stopMonitoring();
        activePublishers.value = activePublishers.value.filter(p => p.id !== leftId);
      }
    } else if (event === "participants") {
      activePublishers.value = msg["participants"].filter((p: any) => p.publisher);
    }
    if (jsep) sfutest.value?.handleRemoteJsep({ jsep });
  }

  function joinRoom() {
    sfutest.value?.send({
      message: {
        request: "join",
        room: janusRoomId.value,
        ptype: "publisher",
        display: `[선생] ${courseStore.currentManagedCourse?.title || "강사"}`,
        pin: pin.value
      }
    });
  }

  // --- [5] 미디어 제어 (카메라/마이크 개별 제어) ---

  async function toggleMic() {
    if (myStream.value && myStream.value.getAudioTracks().length > 0) {
      const audioTrack = myStream.value.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        isMicOn.value = audioTrack.enabled;
        sfutest.value?.send({ message: { request: "configure", audio: isMicOn.value } });
      }
    } else {
      await startPublishing(true, isCamOn.value);
    }
  }

  async function toggleCamera() {
    if (myStream.value && myStream.value.getVideoTracks().length > 0) {
      const videoTrack = myStream.value.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        isCamOn.value = videoTrack.enabled;
        sfutest.value?.send({ message: { request: "configure", video: isCamOn.value } });
      }
    } else {
      await startPublishing(isMicOn.value, true);
    }
  }

  async function startPublishing(audio: boolean, video: boolean) {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      myStream.value = stream;

      const aTrack = stream.getAudioTracks()[0];
      const vTrack = stream.getVideoTracks()[0];
      if (aTrack) aTrack.enabled = audio;
      if (vTrack) vTrack.enabled = video;

      isMicOn.value = audio;
      isCamOn.value = video;

      sfutest.value?.createOffer({
        stream: stream,
        media: { audioRecv: false, videoRecv: false, audioSend: true, videoSend: true },
        success: (jsep: any) => {
          sfutest.value?.send({ 
            message: { request: "configure", audio: isMicOn.value, video: isCamOn.value }, 
            jsep 
          });
        }
      });
    } catch (e) {
      alert("카메라/마이크 권한을 허용해주세요.");
    }
  }

  async function stopPublishing() {
    sfutest.value?.send({ message: { request: "unpublish" } });
    myStream.value?.getTracks().forEach(t => t.stop());
    myStream.value = null;
    isCamOn.value = false;
    isMicOn.value = false;
  }

  async function toggleScreenShare() {
    if (isScreenSharing.value) {
      sfutest.value?.send({ message: { request: "unpublish" } });
      screenStream.value?.getTracks().forEach(t => t.stop());
      screenStream.value = null;
      isScreenSharing.value = false;
    } else {
      try {
        const stream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: true });
        screenStream.value = stream;
        isScreenSharing.value = true;
        sfutest.value?.createOffer({
          stream: stream,
          success: (jsep: any) => {
            sfutest.value?.send({ message: { request: "publish", audio: true, video: true }, jsep });
          }
        });
        stream.getVideoTracks()[0]?.addEventListener('ended', () => {
          if (isScreenSharing.value) toggleScreenShare();
        });
      } catch (e) { console.error(e); }
    }
  }

  // --- [6] 모니터링 (학생 구독) ---

  function startMonitoring(studentId: number) {
    if (monitoringStudentId.value === studentId) return;
    if (monitoringHandle.value) stopMonitoring();
    monitoringStudentId.value = studentId;

    janus.value?.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (handle: any) => {
        monitoringHandle.value = handle;
        handle.send({
          message: { request: "join", room: janusRoomId.value, ptype: "subscriber", feed: studentId, pin: pin.value }
        });
      },
      onmessage: (msg: any, jsep: any) => {
        if (jsep) {
          monitoringHandle.value?.createAnswer({
            jsep,
            media: { audioSend: false, videoSend: false },
            success: (answerJsep: any) => {
              monitoringHandle.value?.send({ 
                message: { request: "start", room: janusRoomId.value }, 
                jsep: answerJsep 
              });
            }
          });
        }
      },
      onremotestream: (stream: MediaStream) => {
        monitoringStream.value = stream;
      }
    });
  }

  function stopMonitoring() {
    monitoringHandle.value?.detach();
    monitoringHandle.value = null;
    monitoringStream.value = null;
    monitoringStudentId.value = null;
  }

  // --- [7] 유틸리티 및 정리 ---

  function startPublisherSync() {
    stopPublisherSync();
    syncInterval = window.setInterval(() => {
      if (sfutest.value && isLiveStarted.value) {
        sfutest.value.send({ message: { request: "listparticipants", room: janusRoomId.value } });
      }
    }, 10000);
  }

  function stopPublisherSync() {
    if (syncInterval) { clearInterval(syncInterval); syncInterval = null; }
  }

  async function fetchParticipants(courseId: string | number) {
    try {
      const { data } = await api.get(`/courses/${courseId}/participants`);
      participants.value = data;
    } catch (e) { console.error(e); }
  }

  function endLiveSession() {
    stopPublisherSync();
    stopMonitoring();
    janus.value?.destroy();
    [myStream, screenStream].forEach(s => {
      s.value?.getTracks().forEach(t => t.stop());
      s.value = null;
    });
    isLiveStarted.value = false;
    isCamOn.value = false;
    isMicOn.value = false;
    isScreenSharing.value = false;
  }

  return {
    janusRoomId, pin, sessionId,
    liveTitle, currentSessionTitle, isLiveStarted, isConnecting, error,
    myStream, screenStream, isCamOn, isMicOn, isScreenSharing,
    activePublishers, monitoringStream, monitoringStudentId, participants,
    initLiveRoom, endLiveSession, toggleCamera, toggleMic, toggleScreenShare, startMonitoring, stopMonitoring
  };
});
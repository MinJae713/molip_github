import { defineStore } from "pinia";
import { ref, computed } from "vue";
import Janus from "janus-gateway";
import adapter from "webrtc-adapter";
import api from "@/api";

import { useUserStore } from "./user";
import { useTrainerCourseStore } from "./trainerCourse";
import { useTrainerSessionStore } from "./trainerSession";
import type { Participant } from "@/types/live";

export const useLiveStore = defineStore("live", () => {
  const courseStore = useTrainerCourseStore();
  const sessionStore = useTrainerSessionStore();
  const userStore = useUserStore();

  // --- [1] 접속 정보 (백엔드 응답 데이터 저장) ---
  const janusRoomId = ref<number>(0);
  const pin = ref<number>(0);  // 🔥 string → number로 수정
  const sessionId = ref<number>(0);
  const isPublished = ref(false);


  // --- [2] Janus 객체 상태 ---
  const janus = ref<any>(null);
  const sfutest = ref<any>(null);  // 메인 핸들 (카메라용)
  const screenHandle = ref<any>(null);  // 🔥 화면공유 전용 핸들
  const monitoringHandle = ref<any>(null); 
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);
  const myid = ref<number | null>(null);
  const screenPublisherId = ref<number | null>(null);  // 🔥 화면공유 publisher ID

  // --- [3] 미디어 및 UI 상태 ---
  const myStream = ref<MediaStream | null>(null);
  const screenStream = ref<MediaStream | null>(null);
  const isCamOn = ref(false);
  const isMicOn = ref(false);
  const isScreenSharing = ref(false);
  
  const isLiveStarted = ref(false);
  const isConnecting = ref(false);
  const error = ref<string | null>(null);
  const isBreakTime = ref(false);  // 🔥 추가: 쉬는 시간 상태

  const monitoringStream = ref<MediaStream | null>(null);
  const monitoringStudentId = ref<number | null>(null);
  const activePublishers = ref<any[]>([]);
  const participants = ref<Participant[]>([]);

  let syncInterval: number | null = null;

  const liveTitle = computed(() => courseStore.currentManagedCourse?.title || "강의 제목 없음");
  const currentSessionTitle = computed(() => sessionStore.currentManagedSession?.title || "세션 정보 없음");

  // --- [4] 초기화 및 연결 로직 ---

  async function initLiveRoom(cId: string | number, sId: string | number) {
    await userStore.fetchUserInfo();

    if (!userStore.token) {
      error.value = "인증 정보가 없습니다.";
      return;
    }

    if (isConnecting.value) return;
    try {
      
      isConnecting.value = true;
      error.value = null;

      // 1. 백엔드 세션 시작 API 호출
      // POST /session/{session_id}/start
      const sessionResponse = await api.post(`/session/${sId}/start`);
      
      janusRoomId.value = sessionResponse.data.janusRoomId;
      pin.value = sessionResponse.data.pin;
      sessionId.value = sessionResponse.data.sessionId;

      // 2. 기초 데이터 로드
      await Promise.all([
        courseStore.setManagedCourse(cId),
        sessionStore.fetchSessionDetail(Number(sId)),
        //fetchParticipants(cId)
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
    const token = userStore.token;
    const server = `wss://i14a309.p.ssafy.io/janus_ws?auth=${token}&session=${sessionId.value}`;

    janus.value = new Janus({
      server,
      success: () => {
        janus.value?.attach({
          plugin: "janus.plugin.videoroom",
          opaqueId: opaqueId.value,

          success: (handle: any) => {
            sfutest.value = handle;
            joinRoom(); // ✅ 여기서 메인 publisher join
          },

          onmessage: handleJanusMessage

        });
      },
      error: (err: any) => {
        console.error("Janus 서버 연결 실패:", err);
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
      
      if (msg["publishers"]) {
        activePublishers.value = msg["publishers"];
      }
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
    
    if (jsep) {
      sfutest.value?.handleRemoteJsep({ jsep });
    }
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

  function publishStream() {
    if (isPublished.value || !myStream.value) return;

    sfutest.value?.createOffer({
      stream: myStream.value,
      media: {
        audioRecv: false,
        videoRecv: false,
        audioSend: isMicOn.value,
        videoSend: isCamOn.value
      },
      success: (jsep: any) => {
        sfutest.value?.send({
          message: {
            request: "publish",
            audio: isMicOn.value,
            video: isCamOn.value
          },
          jsep
        });
        isPublished.value = true;
      }
    });
  }

  // --- [5] 미디어 제어 (카메라/마이크 개별 제어) ---

  async function toggleMic() {
    if (!isMicOn.value) {
      // 🔥 마이크 켜기
      await addMediaTrack(true, false);
      isMicOn.value = true;

      if (!isPublished.value) {
        publishStream(); // 최초 1회
      } else {
        sfutest.value?.send({
          message: { request: "configure", audio: true }
        });
      }
    } else {
      // 🔥 마이크 끄기
      sfutest.value?.send({
        message: { request: "configure", audio: false }
      });

      if (myStream.value) {
        const track = myStream.value.getAudioTracks()[0];
        if (track) track.enabled = false;
      }

      isMicOn.value = false;
    }
  }


  async function toggleCamera() {
    if (!isCamOn.value) {
      // 🔥 카메라 켜기
      await addMediaTrack(false, true);
      isCamOn.value = true;

      if (!isPublished.value) {
        publishStream(); // 최초 1회 publish
      } else {
        sfutest.value?.send({
          message: { request: "configure", video: true }
        });
      }
    } else {
      // 🔥 카메라 끄기
      sfutest.value?.send({
        message: { request: "configure", video: false }
      });

      if (myStream.value) {
        const track = myStream.value.getVideoTracks()[0];
        if (track) track.enabled = false;
      }

      isCamOn.value = false;
    }
  }

  // 🔥 새 함수: 미디어 트랙 추가 (카메라 또는 마이크)
  // ✅ 미디어 트랙 "로컬"에만 추가하는 함수
  async function addMediaTrack(needAudio: boolean, needVideo: boolean) {
    try {
      const constraints: MediaStreamConstraints = {};
      if (needAudio) constraints.audio = true;
      if (needVideo) constraints.video = true;

      const newStream = await navigator.mediaDevices.getUserMedia(constraints);

      if (!myStream.value) {
        myStream.value = newStream;
      } else {
        newStream.getTracks().forEach(track => {
          myStream.value!.addTrack(track);
        });
      }

      if (needAudio) {
        const audioTrack = myStream.value.getAudioTracks()[0];
        if (audioTrack) {
          audioTrack.enabled = true;
          isMicOn.value = true;
        }
      }

      if (needVideo) {
        const videoTrack = myStream.value.getVideoTracks()[0];
        if (videoTrack) {
          videoTrack.enabled = true;
          isCamOn.value = true;
        }
      }

    } catch (e) {
      console.error("미디어 권한 오류:", e);
      alert("카메라/마이크 권한을 허용해주세요.");
    }
  }

  async function toggleScreenShare() {
    if (isScreenSharing.value) {
      screenHandle.value?.send({ message: { request: "unpublish" } });
      screenHandle.value?.detach();
      screenHandle.value = null;

      screenStream.value?.getTracks().forEach(t => t.stop());
      screenStream.value = null;

      isScreenSharing.value = false;
      screenPublisherId.value = null;
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({ video: true });
      screenStream.value = stream;

      janus.value?.attach({
        plugin: "janus.plugin.videoroom",
        opaqueId: `${opaqueId.value}-screen`,

        success: (handle: any) => {
          screenHandle.value = handle;

          handle.send({
            message: {
              request: "join",
              room: janusRoomId.value,
              ptype: "publisher",
              display: `[선생-화면] ${courseStore.currentManagedCourse?.title || "강사"}`,
              pin: pin.value
            }
          });
        },

        onmessage: (msg: any, jsep: any) => {
          if (msg.videoroom === "joined") {
            screenPublisherId.value = msg.id;

            screenHandle.value?.createOffer({
              stream: screenStream.value,   // 🔥 핵심
              media: {
                audioSend: false,
                videoSend: true,
                audioRecv: false,
                videoRecv: false
              },
              success: (jsep: any) => {
                screenHandle.value?.send({
                  message: { request: "publish", video: true, audio: false },
                  jsep
                });
                isScreenSharing.value = true;
              }
            });
          }

          if (jsep) {
            screenHandle.value?.handleRemoteJsep({ jsep });
          }
        }
      });

      stream.getVideoTracks()[0]?.addEventListener("ended", toggleScreenShare);
    } catch (e) {
      console.error("화면공유 실패:", e);
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
          message: { 
            request: "join", 
            room: janusRoomId.value, 
            ptype: "subscriber", 
            feed: studentId, 
            pin: pin.value 
          }
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
        sfutest.value.send({ 
          message: { 
            request: "listparticipants", 
            room: janusRoomId.value 
          } 
        });
      }
    }, 10000);
  }

  function stopPublisherSync() {
    if (syncInterval) { 
      clearInterval(syncInterval); 
      syncInterval = null; 
    }
  }

  async function fetchParticipants(courseId: string | number) {
    try {
      const { data } = await api.get(`/courses/${courseId}/participants`);
      participants.value = data;
    } catch (e) { 
      console.error("참가자 목록 로드 실패:", e); 
    }
  }

  function endLiveSession() {
    stopPublisherSync();
    stopMonitoring();
    
    // 화면공유 핸들 정리
    if (screenHandle.value) {
      screenHandle.value.send({ message: { request: "unpublish" } });
      screenHandle.value.detach();
      screenHandle.value = null;
    }
    
    // 모든 스트림 정리
    [myStream, screenStream].forEach(s => {
      s.value?.getTracks().forEach(t => t.stop());
      s.value = null;
    });
    
    // Janus 연결 종료
    janus.value?.destroy();
    
    // 상태 초기화
    isLiveStarted.value = false;
    isCamOn.value = false;
    isMicOn.value = false;
    isScreenSharing.value = false;
    activePublishers.value = [];
    myid.value = null;
    screenPublisherId.value = null;
  }

  return {
    // 접속 정보
    janusRoomId, 
    pin, 
    sessionId,
    
    // 컴포넌트 표시용
    liveTitle, 
    currentSessionTitle, 
    isLiveStarted, 
    isConnecting, 
    error,
    isBreakTime,  // 🔥 추가
    
    // 미디어 상태
    myStream, 
    screenStream, 
    isCamOn, 
    isMicOn, 
    isScreenSharing,
    screenPublisherId,  // 🔥 추가
    
    // 모니터링
    activePublishers, 
    monitoringStream, 
    monitoringStudentId, 
    participants,
    
    // 함수
    initLiveRoom, 
    endLiveSession, 
    toggleCamera, 
    toggleMic, 
    toggleScreenShare, 
    startMonitoring, 
    stopMonitoring
  };
});
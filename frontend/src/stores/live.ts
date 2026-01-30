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
  const pin = ref<number>(0);  // 🔥 string → number로 수정
  const sessionId = ref<number>(0);

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
      
      // 🔥 카메라/마이크 꺼진 상태로 연결만 수립
      publishEmptyFeed();
      
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

  // 🔥 새 함수: 빈 상태로 연결만 수립
  function publishEmptyFeed() {
    sfutest.value?.createOffer({
      media: { 
        audioRecv: false, 
        videoRecv: false, 
        audioSend: false,  // 일단 꺼진 상태
        videoSend: false 
      },
      success: (jsep: any) => {
        sfutest.value?.send({ 
          message: { 
            request: "publish",  // 처음이니까 publish
            audio: false, 
            video: false 
          }, 
          jsep 
        });
      },
      error: (error: any) => {
        console.error("Empty feed publish 실패:", error);
      }
    });
  }

  // --- [5] 미디어 제어 (카메라/마이크 개별 제어) ---

  async function toggleMic() {
    if (isMicOn.value) {
      // 마이크 끄기
      if (myStream.value) {
        const audioTrack = myStream.value.getAudioTracks()[0];
        if (audioTrack) {
          audioTrack.enabled = false;
          isMicOn.value = false;
          sfutest.value?.send({ message: { request: "configure", audio: false } });
        }
      }
    } else {
      // 마이크 켜기
      if (!myStream.value || myStream.value.getAudioTracks().length === 0) {
        // 스트림이 없거나 오디오 트랙이 없으면 새로 시작
        await addMediaTrack(true, false);
      } else {
        // 이미 오디오 트랙 있으면 활성화만
        const audioTrack = myStream.value.getAudioTracks()[0];
        audioTrack.enabled = true;
        isMicOn.value = true;
        sfutest.value?.send({ message: { request: "configure", audio: true } });
      }
    }
  }

  async function toggleCamera() {
    if (isCamOn.value) {
      // 카메라 끄기
      if (myStream.value) {
        const videoTrack = myStream.value.getVideoTracks()[0];
        if (videoTrack) {
          videoTrack.enabled = false;
          isCamOn.value = false;
          sfutest.value?.send({ message: { request: "configure", video: false } });
        }
      }
    } else {
      // 카메라 켜기
      if (!myStream.value || myStream.value.getVideoTracks().length === 0) {
        // 스트림이 없거나 비디오 트랙이 없으면 새로 시작
        await addMediaTrack(false, true);
      } else {
        // 이미 비디오 트랙 있으면 활성화만
        const videoTrack = myStream.value.getVideoTracks()[0];
        videoTrack.enabled = true;
        isCamOn.value = true;
        sfutest.value?.send({ message: { request: "configure", video: true } });
      }
    }
  }

  // 🔥 새 함수: 미디어 트랙 추가 (카메라 또는 마이크)
  async function addMediaTrack(needAudio: boolean, needVideo: boolean) {
    try {
      const constraints: any = {};
      if (needAudio) constraints.audio = true;
      if (needVideo) constraints.video = true;
      
      const newStream = await navigator.mediaDevices.getUserMedia(constraints);
      
      // 기존 스트림에 새 트랙 추가
      if (!myStream.value) {
        myStream.value = newStream;
      } else {
        newStream.getTracks().forEach(track => {
          myStream.value!.addTrack(track);
        });
      }

      // 트랙 상태 업데이트
      if (needAudio) {
        const aTrack = myStream.value.getAudioTracks()[0];
        if (aTrack) {
          aTrack.enabled = true;
          isMicOn.value = true;
        }
      }
      if (needVideo) {
        const vTrack = myStream.value.getVideoTracks()[0];
        if (vTrack) {
          vTrack.enabled = true;
          isCamOn.value = true;
        }
      }

      // 🔥 이미 publish 했으니까 configure 사용!
      sfutest.value?.createOffer({
        stream: myStream.value,
        media: { 
          audioRecv: false, 
          videoRecv: false, 
          audioSend: true, 
          videoSend: true 
        },
        success: (jsep: any) => {
          sfutest.value?.send({ 
            message: { 
              request: "configure", 
              audio: isMicOn.value, 
              video: isCamOn.value 
            }, 
            jsep 
          });
        }
      });
    } catch (e) {
      console.error("미디어 권한 오류:", e);
      alert("카메라/마이크 권한을 허용해주세요.");
    }
  }

  async function toggleScreenShare() {
    if (isScreenSharing.value) {
      // 화면공유 중지 (카메라는 유지!)
      if (screenHandle.value) {
        screenHandle.value.send({ message: { request: "unpublish" } });
        screenHandle.value.detach();
        screenHandle.value = null;
      }
      screenStream.value?.getTracks().forEach(t => t.stop());
      screenStream.value = null;
      isScreenSharing.value = false;
      screenPublisherId.value = null;
    } else {
      // 화면공유 시작 (카메라와 별개로!)
      try {
        const stream = await navigator.mediaDevices.getDisplayMedia({ 
          video: true, 
          audio: true 
        });
        
        screenStream.value = stream;
        
        // 🔥 새로운 핸들 생성 (화면공유 전용)
        janus.value?.attach({
          plugin: "janus.plugin.videoroom",
          opaqueId: `${opaqueId.value}-screen`,
          success: (handle: any) => {
            screenHandle.value = handle;
            
            // 화면공유 방에 입장
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
            const event = msg["videoroom"];
            if (event === "joined") {
              screenPublisherId.value = msg["id"];
              
              // 화면공유 스트림 송출
              screenHandle.value?.createOffer({
                stream: stream,
                media: { 
                  audioRecv: false, 
                  videoRecv: false, 
                  audioSend: true, 
                  videoSend: true 
                },
                success: (jsep: any) => {
                  screenHandle.value?.send({ 
                    message: { 
                      request: "publish",  // 새 핸들이니까 publish
                      audio: true, 
                      video: true 
                    }, 
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
        
        // 화면공유 종료 버튼 눌렀을 때 자동 복귀
        stream.getVideoTracks()[0]?.addEventListener('ended', () => {
          if (isScreenSharing.value) {
            toggleScreenShare();
          }
        });
      } catch (e) {
        console.error("화면공유 실패:", e);
        alert("화면공유 권한을 허용해주세요.");
      }
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
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import Janus from "janus-gateway";
import adapter from "webrtc-adapter";
import api from "@/api";

// [스토어 주입] 강사 전용 코스 및 세션 정보 활용
import { useTrainerCourseStore } from "./trainerCourse";
import { useTrainerSessionStore } from "./trainerSession";
import type { Participant } from "@/types/live";

export const useLiveStore = defineStore("live", () => {
  const courseStore = useTrainerCourseStore();
  const sessionStore = useTrainerSessionStore();

  // --- [1] 접속 정보 (다른 컴포넌트와 공유) ---
  const janusRoomId = ref(0);
  const pin = ref("");
  const sessionId = ref(0);

  // --- [2] Janus 핵심 객체 상태 ---
  const janus = ref<any>(null);
  const sfutest = ref<any>(null); // 강사 본인 송출용 핸들
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);
  const myid = ref<number | null>(null);

  // --- [3] 미디어 및 UI 상태 ---
  const myStream = ref<MediaStream | null>(null);      // 카메라+마이크 스트림
  const screenStream = ref<MediaStream | null>(null);  // 화면 공유 스트림
  const isCamOn = ref(false);
  const isMicOn = ref(false);
  const isScreenSharing = ref(false);
  
  const isLiveStarted = ref(false);
  const isBreakTime = ref(false);
  const recordingId = ref<string | null>(null);
  const mainStreamActive = ref(false); // 메인 모니터링 창 활성화 여부
  const aiAnalysisActive = ref(true);

  // Computed: 코스 스토어에서 제목 실시간 참조
  const liveTitle = computed(() => courseStore.currentManagedCourse?.title || "강의 제목 없음");
  const currentSessionTitle = computed(() => sessionStore.currentManagedSession?.title || "세션 정보 없음");

  // --- [4] 참여자 및 모니터링 상태 ---
  const participants = ref<Participant[]>([]);           // DB 참여자 목록
  const activePublishers = ref<any[]>([]);               // 현재 카메라 켠 실제 접속자들
  const monitoringStream = ref<MediaStream | null>(null); // 선택된 학생 영상
  const monitoringHandle = ref<any>(null);               // 모니터링용 구독 핸들
  const monitoringStudentId = ref<number | null>(null);   // 모니터링 중인 학생 ID

  // --- [5] 초기화 및 연결 액션 ---

  /** 라이브 입장 시 실행: 데이터 로드 후 Janus 엔진 가동 */
  async function initLiveRoom(cId: string | number, sId: number, rId: number, roomPin: string) {
    try {
      // 1. 전달받은 방 정보 저장
      sessionId.value = sId;
      janusRoomId.value = rId;
      pin.value = roomPin;

      // 2. 외부 스토어 데이터 동기화
      await courseStore.setManagedCourse(cId);
      await sessionStore.fetchSessionDetail(sId);
      await fetchParticipants(cId);
      
      // 3. Janus 엔진 초기화
      Janus.init({
        debug: "all",
        dependencies: Janus.useDefaultDependencies({ adapter }),
        callback: () => {
          if (!Janus.isWebrtcSupported()) {
            alert("이 브라우저는 화상 통화를 지원하지 않습니다.");
            return;
          }
          createJanusInstance();
        }
      });
    } catch (error) {
      console.error("라이브 초기화 실패:", error);
    }
  }

  /** 서버 연결 인스턴스 생성 및 비디오룸 플러그인 부착 */
  function createJanusInstance() {
    const token = localStorage.getItem("accessToken");
    const server = `${import.meta.env.VITE_JANUS_SERVER_URL}?auth=${token}`;

    janus.value = new Janus({
      server: server,
      success: () => {
        janus.value.attach({
          plugin: "janus.plugin.videoroom",
          opaqueId: opaqueId.value,
          success: (pluginHandle: any) => {
            sfutest.value = pluginHandle;
            joinRoom();
          },
          onmessage: (msg: any, jsep: any) => {
            const event = msg["videoroom"];
            if (event === "joined") {
              myid.value = msg["id"];
              isLiveStarted.value = true;
              if (msg["publishers"]) activePublishers.value = msg["publishers"];
            } else if (event === "event") {
              if (msg["publishers"]) {
                activePublishers.value = [...activePublishers.value, ...msg["publishers"]];
              } else if (msg["leaving"] || msg["unpublished"]) {
                const leftId = Number(msg["leaving"] || msg["unpublished"]);
                if (monitoringStudentId.value === leftId) stopMonitoring();
                activePublishers.value = activePublishers.value.filter(p => p.id !== leftId);
              }
            }
            if (jsep) sfutest.value.handleRemoteJsep({ jsep: jsep });
          },
          onlocalstream: (stream: MediaStream) => {
            myStream.value = stream;
          },
          oncleanup: () => {
            myStream.value = null;
            isLiveStarted.value = false;
          }
        });
      },
      error: (err: any) => console.error("Janus 연결 실패", err)
    });
  }

  /** 비디오룸 입장 요청 (저장된 룸번호와 핀 사용) */
  function joinRoom() {
    const register = {
      request: "join",
      room: janusRoomId.value,
      ptype: "publisher",
      display: `[선생] ${courseStore.currentManagedCourse?.title || "강사"}`,
      pin: pin.value
    };
    sfutest.value?.send({ message: register });
  }

  // --- [6] 모니터링 제어 (학생 개별 시청) ---

  /** 목록에서 학생 클릭 시 해당 학생의 영상만 구독 시작 */
  function startMonitoring(studentId: number) {
    if (monitoringHandle.value) stopMonitoring();
    monitoringStudentId.value = studentId;

    janus.value.attach({
      plugin: "janus.plugin.videoroom",
      opaqueId: opaqueId.value,
      success: (pluginHandle: any) => {
        monitoringHandle.value = pluginHandle;
        const subscribe = {
          request: "join",
          room: janusRoomId.value,
          ptype: "subscriber",
          feed: studentId,
          pin: pin.value
        };
        monitoringHandle.value.send({ message: subscribe });
      },
      onmessage: (msg: any, jsep: any) => {
        if (jsep) {
          monitoringHandle.value.createAnswer({
            jsep: jsep,
            media: { audioSend: false, videoSend: false },
            success: (jsep: any) => {
              monitoringHandle.value.send({ 
                message: { request: "start", room: janusRoomId.value }, 
                jsep 
              });
            }
          });
        }
      },
      onremotestream: (stream: MediaStream) => {
        monitoringStream.value = stream;
        mainStreamActive.value = true;
      }
    });
  }

  /** 모니터링 중단 및 리소스 해제 */
  function stopMonitoring() {
    if (monitoringHandle.value) {
      monitoringHandle.value.detach();
      monitoringHandle.value = null;
    }
    monitoringStream.value = null;
    monitoringStudentId.value = null;
    mainStreamActive.value = false;
  }

  // --- [7] 강사 미디어 제어 (카메라, 마이크, 화면공유) ---

  /** 강사 카메라/마이크 토글 */
  async function toggleCamera() {
    try {
      if (isCamOn.value) {
        sfutest.value.send({ message: { request: "unpublish" } });
        myStream.value?.getTracks().forEach(t => t.stop());
        myStream.value = null;
        isCamOn.value = false;
        isMicOn.value = false;
      } else {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        isCamOn.value = true;
        isMicOn.value = true;
        sfutest.value.createOffer({
          stream: stream,
          media: { audioRecv: false, videoRecv: false, audioSend: true, videoSend: true },
          success: (jsep: any) => {
            const publish = { request: "configure", audio: true, video: true };
            sfutest.value.send({ message: publish, jsep: jsep });
          }
        });
      }
    } catch (e) {
      console.error("카메라 권한 오류", e);
    }
  }

  /** 마이크만 개별 제어 */
  function toggleMic() {
    if (myStream.value) {
      const audioTrack = myStream.value.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        isMicOn.value = audioTrack.enabled;
      }
    }
  }

  /** 화면 공유 시작/중지 */
  async function toggleScreenShare() {
    try {
      if (isScreenSharing.value) {
        sfutest.value.send({ message: { request: "unpublish" } });
        screenStream.value?.getTracks().forEach(t => t.stop());
        screenStream.value = null;
        isScreenSharing.value = false;
      } else {
        const stream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: true });
        screenStream.value = stream;
        isScreenSharing.value = true;

        sfutest.value.createOffer({
          stream: stream,
          success: (jsep: any) => {
            const publish = { request: "publish", audio: true, video: true };
            sfutest.value.send({ message: publish, jsep: jsep });
          }
        });
        // 브라우저 기본 공유 중지 대응
        stream.getVideoTracks()[0].onended = () => { if (isScreenSharing.value) toggleScreenShare(); };
      }
    } catch (err) {
      console.error("화면 공유 실패", err);
    }
  }

  // --- [8] 기타 관리 기능 ---

  /** DB 참여자 목록 로드 */
  async function fetchParticipants(courseId: string | number) {
    try {
      const response = await api.get(`/courses/${courseId}/participants`);
      participants.value = response.data;
    } catch (error) {
      console.error("참여자 로드 실패:", error);
    }
  }

  /** 참여자 집중도 점수 업데이트 (AI 연동용) */
  function updateParticipantScore(id: string, score: number) {
    const p = participants.value.find((p) => p.id === id);
    if (p) p.concentrationScore = score;
  }

  /** 라이브 종료 및 모든 리소스 정리 */
  function endLiveSession() {
    stopMonitoring();
    if (janus.value) janus.value.destroy();
    
    [myStream, screenStream].forEach(s => {
      s.value?.getTracks().forEach(t => t.stop());
      s.value = null;
    });

    isMicOn.value = false;
    isCamOn.value = false;
    isScreenSharing.value = false;
    isLiveStarted.value = false;
  }

  return {
    // 접속 정보
    janusRoomId, pin, sessionId,
    // 상태
    liveTitle, currentSessionTitle, isBreakTime, isLiveStarted, recordingId,
    isMicOn, isCamOn, myStream, isScreenSharing, screenStream, mainStreamActive,
    aiAnalysisActive, participants, activePublishers, monitoringStream, monitoringStudentId,
    // 액션
    initLiveRoom, endLiveSession, toggleCamera, toggleMic, toggleScreenShare,
    updateParticipantScore, fetchParticipants, startMonitoring, stopMonitoring
  };
});
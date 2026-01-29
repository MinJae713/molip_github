import { defineStore } from "pinia";
import { ref } from "vue";
import Janus from "janus-gateway";
import adapter from "webrtc-adapter";
import api from "@/api";

// [스토어 주입] 강사 전용 코스/세션 정보 활용
import { useTrainerCourseStore } from "./trainerCourse";
import { useTrainerSessionStore } from "./trainerSession";
import type { Participant } from "@/types/live";

export const useLiveStore = defineStore("live", () => {
  const courseStore = useTrainerCourseStore();
  const sessionStore = useTrainerSessionStore();

  // --- [1] 상태 (State) ---

  // Janus 핵심 객체
  const janus = ref<any>(null);
  const sfutest = ref<any>(null);
  const opaqueId = ref(`videoroom-${Janus.randomString(12)}`);
  const myid = ref<number | null>(null);

  // 강의 및 세션 메타 정보
  const liveTitle = ref("");
  const currentSessionTitle = ref("");
  const isBreakTime = ref(false);
  const isLiveStarted = ref(false);
  const recordingId = ref<string | null>(null);

  // 미디어 상태 (카메라, 마이크, 화면공유)
  const isMicOn = ref(false);
  const isCamOn = ref(false);
  const myStream = ref<MediaStream | null>(null);     // 내 로컬 영상
  const isScreenSharing = ref(false);
  const screenStream = ref<MediaStream | null>(null); // 내 공유 화면
  const mainStreamActive = ref(false);                // 메인 캔버스 활성화 여부

  // 참여자 및 AI 분석 상태
  const aiAnalysisActive = ref(true);
  const participants = ref<Participant[]>([]);
  const selectedStudent = ref<Participant | null>(null);

  // --- [2] 액션: 라이브 초기화 및 Janus 연결 (샘플 코드 규격) ---

  /** 라이브 입장 시 실행: 데이터 로드 후 Janus 엔진 가동 */
  async function initLiveRoom(courseId: string | number, sessionId: string | number) {
    try {
      // 1. 강사 스토어 데이터 로드
      await courseStore.setManagedCourse(courseId);
      await sessionStore.fetchSessionDetail(sessionId);

      liveTitle.value = courseStore.currentManagedCourse?.title || "강의 제목 없음";
      currentSessionTitle.value = sessionStore.currentManagedSession?.title || "세션 정보 없음";

      // 2. 참여자 목록 로드
      await fetchParticipants(courseId);

      // 3. Janus 초기화 (샘플 코드 initJanus 규격)
      initJanusEngine();
    } catch (error) {
      console.error("라이브 초기화 실패:", error);
    }
  }

  function initJanusEngine() {
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
  }

  /** 서버 연결 인스턴스 생성 */
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
            joinRoom(); // 입장 요청
          },
          onmessage: (msg: any, jsep: any) => onJanusMessage(msg, jsep),
          onlocalstream: (stream: MediaStream) => {
            // 강사 카메라 영상이 로컬에서 준비됨
            myStream.value = stream;
          },
          oncleanup: () => {
            myStream.value = null;
            mainStreamActive.value = false;
          }
        });
      },
      error: (err: any) => console.error("Janus 연결 실패", err)
    });
  }

  /** 비디오룸 입장 요청 */
  function joinRoom() {
      // 1. sfutest.value가 있는지 체크 (TS 에러 방지)
      if (!sfutest.value) {
        console.error("Janus 플러그인이 연결되지 않았습니다.");
        return;
      }

      // 2. 강사 이름 결정 로직 
      // (이미 로그인 스토어 등에서 가져오거나, 없다면 기본값 사용)
      const instructorDisplayName = courseStore.currentManagedCourse?.title 
        ? `[선생] ${courseStore.currentManagedCourse.title} 강사` 
        : "[선생] 강사";

      const register = {
        request: "join",
        room: 1234, // 실제 sessionStore에서 관리하는 번호가 있다면 교체
        ptype: "publisher",
        display: instructorDisplayName, // 강사 본인의 이름으로 설정
        pin: "1234"
      };

      // 3. Optional Chaining(?.)으로 한 번 더 안전하게 실행
      sfutest.value?.send({ message: register });
    }

  // --- [3] 메시지 및 미디어 제어 ---

  function onJanusMessage(msg: any, jsep: any) {
    const event = msg["videoroom"];
    if (event === "joined") {
      myid.value = msg["id"];
      isLiveStarted.value = true;
    }

    if (jsep) {
      sfutest.value.handleRemoteJsep({ jsep: jsep });
    }
  }

  /** 카메라/마이크 전송 시작 */
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

  /** 마이크만 제어 (로컬 트랙 enable/disable) */
  function toggleMic() {
    if (myStream.value) {
      const audioTrack = myStream.value.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        isMicOn.value = audioTrack.enabled;
      }
    }
  }

  /** 화면 공유 제어 */
  async function toggleScreenShare() {
    try {
      if (isScreenSharing.value) {
        sfutest.value.send({ message: { request: "unpublish" } });
        screenStream.value?.getTracks().forEach(t => t.stop());
        screenStream.value = null;
        isScreenSharing.value = false;
        mainStreamActive.value = false;
      } else {
        const stream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: true });
        screenStream.value = stream;
        isScreenSharing.value = true;
        mainStreamActive.value = true;

        sfutest.value.createOffer({
          stream: stream,
          success: (jsep: any) => {
            const publish = { request: "publish", audio: true, video: true };
            sfutest.value.send({ message: publish, jsep: jsep });
          }
        });

        stream.getVideoTracks()[0].onended = () => {
          if (isScreenSharing.value) toggleScreenShare();
        };
      }
    } catch (err) {
      console.error("화면 공유 실패", err);
    }
  }

  // --- [4] 참여자 관리 및 기타 액션 ---

  async function fetchParticipants(courseId: string | number) {
    try {
      const response = await api.get(`/courses/${courseId}/participants`);
      participants.value = response.data;
    } catch (error) {
      console.error("참여자 로드 실패 (Mock 데이터 유지):", error);
      // 데이터가 없을 때 UI 깨짐 방지를 위한 더미
      if (participants.value.length === 0) {
        participants.value = [{ id: "1", name: "김철수", avatar: "", isCameraOn: true, isMicOn: true, concentrationScore: 92, status: "focus" }];
      }
    }
  }

  function updateParticipantScore(id: string, score: number) {
    const p = participants.value.find((p) => p.id === id);
    if (p) p.concentrationScore = score;
  }

  function selectStudent(participant: Participant) {
    selectedStudent.value = participant;
    // TODO: 학생 ID로 Janus Subscribe 핸들 연결 로직 추가
  }

  /** 라이브 종료 */
  async function endLiveSession() {
    if (janus.value) janus.value.destroy();
    
    [myStream, screenStream].forEach(s => {
      s.value?.getTracks().forEach(t => t.stop());
      s.value = null;
    });

    isMicOn.value = false;
    isCamOn.value = false;
    isScreenSharing.value = false;
    isLiveStarted.value = false;
    selectedStudent.value = null;
  }

  return {
    // 상태
    liveTitle, currentSessionTitle, isBreakTime, isLiveStarted, recordingId,
    isMicOn, isCamOn, myStream, isScreenSharing, screenStream, mainStreamActive,
    aiAnalysisActive, participants, selectedStudent,
    // 액션
    initLiveRoom, endLiveSession, toggleCamera, toggleMic, toggleScreenShare,
    updateParticipantScore, fetchParticipants, selectStudent,
  };
});
import type { Participant } from "@/types/live";
import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";
import { useLearnerCourseStore } from "./learnerCourse";

export const useLiveStore = defineStore("live", () => {
  const courseStore = useLearnerCourseStore();

  // --- [1] 상태 (State) ---

  // 강의 및 세션 정보
  const liveTitle = ref("");
  const currentSessionTitle = ref("");
  const isBreakTime = ref(false);
  const isLiveStarted = ref(false); // 수업 시작 여부
  const recordingId = ref<string | null>(null); // 백엔드에서 받은 녹화 고유 ID
  // 미디어 상태 (카메라, 마이크, 화면공유)
  const isMicOn = ref(false);
  const isCamOn = ref(false);
  const myStream = ref<MediaStream | null>(null);
  const isScreenSharing = ref(false);
  const screenStream = ref<MediaStream | null>(null);

  // 참여자 및 AI 분석 상태
  const aiAnalysisActive = ref(true);
  const participants = ref<Participant[]>([
    {
      id: "1",
      name: "김철수",
      avatar: "https://i.pravatar.cc/150?u=1",
      isCameraOn: true,
      isMicOn: true,
      concentrationScore: 92,
      status: "focus",
    },
    {
      id: "2",
      name: "이영희",
      avatar: "https://i.pravatar.cc/150?u=2",
      isCameraOn: false,
      isMicOn: true,
      concentrationScore: 45,
      status: "distracted",
    },
  ]);
  const selectedStudent = ref<Participant | null>(null);

  // --- [2] 액션: 라이브 초기화 및 종료 ---

  /** 라이브 입장 시 실행: 강의 및 세션 정보를 세팅합니다. */
  async function initLiveRoom(
    courseId: string | number,
    sessionId: string | number,
  ) {
    try {
      // 1. 코스 정보 로드
      if (
        !courseStore.courseInfo ||
        String(courseStore.courseInfo.course_id) !== String(courseId)
      ) {
        await courseStore.fetchCourseInfo(courseId);
      }

      // 2. 세션 리스트 로드 및 현재 세션 찾기
      const sessions = await courseStore.fetchCourseSessions(courseId);
      const currentSession = sessions.find(
        (s: any) =>
          String(s.session_id) === String(sessionId) ||
          String(s.id) === String(sessionId),
      );

      // 3. 라이브 헤더용 데이터 할당
      liveTitle.value = courseStore.courseInfo?.title || "강의 제목 없음";
      currentSessionTitle.value = currentSession?.title || "세션 정보 없음";

      // 4. 참여자 목록 로드
      await fetchParticipants(courseId);
    } catch (error) {
      console.error("라이브 초기화 실패:", error);
      liveTitle.value = "라이브 강의 (연결 실패)";
      currentSessionTitle.value = "세션 정보를 불러올 수 없습니다.";
    }
  }

  /** 라이브 세션 종료: 모든 미디어 자원을 해제하고 상태를 초기화합니다. */
  async function endLiveSession() {
    try {
      if (myStream.value) {
        myStream.value.getTracks().forEach((track) => track.stop());
        myStream.value = null;
      }
      if (screenStream.value) {
        screenStream.value.getTracks().forEach((track) => track.stop());
        screenStream.value = null;
      }

      isMicOn.value = false;
      isCamOn.value = false;
      isScreenSharing.value = false;
      selectedStudent.value = null;
      isBreakTime.value = false;

      console.log("라이브 세션이 안전하게 종료되었습니다.");
    } catch (error) {
      console.error("세션 종료 중 에러 발생:", error);
    }
  }

  // --- [3] 액션: 미디어 제어 ---

  async function toggleCamera() {
    try {
      if (isCamOn.value) {
        myStream.value?.getVideoTracks().forEach((track) => track.stop());
        isCamOn.value = false;
      } else {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: isMicOn.value,
        });
        myStream.value = stream;
        isCamOn.value = true;
      }
    } catch (error) {
      console.error("카메라 연결 실패:", error);
      alert("카메라 권한을 허용해주세요.");
    }
  }

  async function toggleMic() {
    if (myStream.value) {
      const audioTrack = myStream.value.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        isMicOn.value = audioTrack.enabled;
      }
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });
        myStream.value = stream;
        isMicOn.value = true;
      } catch (error) {
        console.error("마이크 연결 실패:", error);
      }
    }
  }

  async function toggleScreenShare() {
    try {
      if (isScreenSharing.value) {
        screenStream.value?.getTracks().forEach((track) => track.stop());
        screenStream.value = null;
        isScreenSharing.value = false;
      } else {
        const stream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true,
        });
        const videoTrack = stream.getVideoTracks()[0];
        if (videoTrack) {
          videoTrack.onended = () => {
            isScreenSharing.value = false;
            screenStream.value = null;
          };
        }
        screenStream.value = stream;
        isScreenSharing.value = true;
      }
    } catch (err) {
      console.error("화면 공유 실패:", err);
    }
  }

  // --- [4] 액션: 참여자 관리 ---

  async function fetchParticipants(courseId: string | number) {
    try {
      const response = await api.get(`/courses/${courseId}/participants`);
      participants.value = response.data;
    } catch (error) {
      console.error("참여자 목록 로드 실패 (Mock 데이터 유지):", error);
    }
  }

  function updateParticipantScore(id: string, score: number) {
    const p = participants.value.find((p) => p.id === id);
    if (p) p.concentrationScore = score;
  }

  function selectStudent(participant: Participant) {
    selectedStudent.value = participant;
    // TODO: Janus subscribe logic 추가 예정
  }

  return {
    liveTitle,
    currentSessionTitle,
    isBreakTime,
    isMicOn,
    isCamOn,
    myStream,
    isScreenSharing,
    screenStream,
    aiAnalysisActive,
    participants,
    selectedStudent,
    initLiveRoom,
    endLiveSession,
    toggleCamera,
    toggleMic,
    toggleScreenShare,
    updateParticipantScore,
    fetchParticipants,
    selectStudent,
    isLiveStarted,
    recordingId,
  };
});

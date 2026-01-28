import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '@/api';
import { useCourseStore } from './course';
import { useQuizStore } from './quiz'; // 퀴즈 스토어와 연동
import type { Participant } from '@/types/live';

export const useLiveStore = defineStore('live', () => {
  const courseStore = useCourseStore();
  const quizStore = useQuizStore();

  // --- [1] 상태 (State) ---
  const liveTitle = ref('');
  const currentSessionTitle = ref('');
  const isLiveStarted = ref(false);
  const isBreakTime = ref(false);

  // 미디어 상태
  const isMicOn = ref(false);
  const isCamOn = ref(false);
  const myStream = ref<MediaStream | null>(null);
  const isScreenSharing = ref(false);
  const screenStream = ref<MediaStream | null>(null);

  // 참여자 및 분석
  const participants = ref<Participant[]>([]);
  const selectedStudent = ref<Participant | null>(null);
  const aiAnalysisActive = ref(true);

  // 실시간 통신 (WS & STT)
  const socket = ref<WebSocket | null>(null);
  const isRecordingVoice = ref(false);
  let mediaRecorder: MediaRecorder | null = null;
  let voiceInterval: number | null = null;

  // --- [2] computed (Getters) ---
  const participantCount = computed(() => participants.value.length);

  // --- [3] 액션: 라이브 생명주기 ---

  /** 라이브 방 입장 및 초기화 */
  async function initLiveRoom(courseId: string | number, sessionId: string | number) {
    try {
      if (!courseStore.courseInfo) await courseStore.fetchCourseInfo(courseId);
      const sessions = await courseStore.fetchCourseSessions(courseId);
      const currentSession = sessions.find((s: any) => String(s.session_id) === String(sessionId));

      liveTitle.value = courseStore.courseInfo?.title || '실시간 강의';
      currentSessionTitle.value = currentSession?.title || '세션 정보 없음';

      await fetchParticipants(courseId);
      connectSocket(); // 입장 시 자동 소켓 연결
    } catch (error) {
      console.error("라이브 초기화 실패:", error);
    }
  }

  /** 라이브 종료 및 자원 해제 */
  function endLiveSession() {
    stopVoiceRecording();
    [myStream.value, screenStream.value].forEach(stream => {
      stream?.getTracks().forEach(track => track.stop());
    });

    if (socket.value) {
      socket.value.close();
      socket.value = null;
    }

    isLiveStarted.value = false;
    myStream.value = null;
    screenStream.value = null;
  }

  // --- [4] 액션: 미디어 제어 (Janus 연동 지점) ---

  async function toggleCamera() {
    try {
      if (isCamOn.value) {
        myStream.value?.getVideoTracks().forEach(t => t.stop());
        isCamOn.value = false;
      } else {
        myStream.value = await navigator.mediaDevices.getUserMedia({ video: true, audio: isMicOn.value });
        isCamOn.value = true;
      }
    } catch (e) { alert("카메라 권한이 필요합니다."); }
  }

  async function toggleMic() {
    const audioTrack = myStream.value?.getAudioTracks()[0];
    if (audioTrack) {
      audioTrack.enabled = !audioTrack.enabled;
      isMicOn.value = audioTrack.enabled;
    }
  }

  // --- [5] 액션: 웹소켓 & STT & 퀴즈 엔진 ---

  function connectSocket() {
    if (socket.value?.readyState === WebSocket.OPEN) return;

    const wsUrl = `${import.meta.env.VITE_WS_URL}/live`;
    socket.value = new WebSocket(wsUrl);

    socket.value.onmessage = (event) => {
      const { type, payload } = JSON.parse(event.data);

      switch (type) {
        case 'QUIZ_GENERATED':
          // AI가 퀴즈 생성 시 퀴즈 스토어의 추천 리스트에 추가
          quizStore.recommendedQuizzes.unshift(payload);
          // 알림 로직 추가 가능 (예: Toast 메시지)
          break;
        case 'STUDENT_CONCENTRATION':
          updateParticipantScore(payload.userId, payload.score);
          break;
      }
    };
  }

  /** 강사 음성 10초 단위 서버 전송 (STT 엔진용) */
  async function startVoiceRecording() {
    if (isRecordingVoice.value) return;

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });

      mediaRecorder.ondataavailable = async (e) => {
        if (e.data.size > 0 && socket.value?.readyState === WebSocket.OPEN) {
          // Binary 데이터로 전송하여 효율화
          socket.value.send(e.data);
        }
      };

      mediaRecorder.start();
      isRecordingVoice.value = true;

      // 10초마다 chunk 생성 강제 실행
      voiceInterval = window.setInterval(() => {
        mediaRecorder?.requestData();
      }, 10000);

    } catch (err) { console.error('STT 시작 실패:', err); }
  }

  function stopVoiceRecording() {
    if (voiceInterval) clearInterval(voiceInterval);
    mediaRecorder?.stop();
    isRecordingVoice.value = false;
  }

  /** 최종 확정된 퀴즈 수강생들에게 브로드캐스팅 */
  function publishQuiz(quizData: any) {
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) return;

    socket.value.send(JSON.stringify({
      type: 'PUBLISH_QUIZ',
      payload: {
        ...quizData,
        sentAt: new Date().toISOString()
      }
    }));
  }

  // --- [6] 액션: 참여자 관리 ---
  async function fetchParticipants(courseId: string | number) {
    try {
      const { data } = await api.get(`/courses/${courseId}/participants`);
      participants.value = data;
    } catch (e) { console.error("참여자 로드 실패"); }
  }

  function updateParticipantScore(id: string, score: number) {
    const p = participants.value.find(p => p.id === id);
    if (p) {
      p.concentrationScore = score;
      p.status = score > 70 ? 'focus' : 'distracted';
    }
  }

  return {
    liveTitle, currentSessionTitle, isLiveStarted, isBreakTime,
    isMicOn, isCamOn, myStream, isScreenSharing, screenStream,
    participants, selectedStudent, participantCount, aiAnalysisActive,
    initLiveRoom, endLiveSession, toggleCamera, toggleMic,
    startVoiceRecording, stopVoiceRecording, publishQuiz,
    // [NEW] Exports
    socket,
    isRecordingVoice
  };
});

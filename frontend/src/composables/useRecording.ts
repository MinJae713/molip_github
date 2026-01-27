import { ref, onUnmounted } from 'vue';

export function useRecording() {
  const recordingTime = ref('00:00:00');
  const isRecording = ref(false);
  const recordingStartTime = ref<number | null>(null);
  let timer: ReturnType<typeof setInterval> | null = null;

  /**
   * 시간을 00:00:00 포맷으로 업데이트하는 내부 함수
   */
  const update = () => {
    if (!recordingStartTime.value) return;

    const now = Date.now();
    const diff = Math.floor((now - recordingStartTime.value) / 1000);

    const h = String(Math.floor(diff / 3600)).padStart(2, '0');
    const m = String(Math.floor((diff % 3600) / 60)).padStart(2, '0');
    const s = String(diff % 60).padStart(2, '0');

    recordingTime.value = `${h}:${m}:${s}`;
  };

  /**
   * 녹화 시작: 버튼을 클릭했을 때 호출됩니다.
   */
  const startRecording = () => {
    if (isRecording.value) return; // 이미 녹화 중이면 중복 방지

    recordingStartTime.value = Date.now();
    isRecording.value = true;
    
    // 1초마다 업데이트 실행
    timer = setInterval(update, 1000);
    console.log("Recording started at:", new Date(recordingStartTime.value).toLocaleTimeString());
  };

  /**
   * 녹화 중지: 수업 종료 시 호출됩니다.
   */
  const stopRecording = () => {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
    isRecording.value = false;
    // 필요한 경우 여기서 최종 recordingTime을 반환하거나 로그를 남길 수 있습니다.
    console.log("Recording stopped at total duration:", recordingTime.value);
  };

  /**
   * 리셋: 다음 강의나 재시작을 위해 초기화합니다.
   */
  const resetRecording = () => {
    stopRecording();
    recordingTime.value = '00:00:00';
    recordingStartTime.value = null;
  };

  // 컴포넌트가 파괴될 때 타이머를 확실히 제거하여 메모리 누수를 방지합니다.
  onUnmounted(() => {
    stopRecording();
  });

  return {
    recordingTime,
    isRecording,
    recordingStartTime,
    startRecording,
    stopRecording,
    resetRecording
  };
}
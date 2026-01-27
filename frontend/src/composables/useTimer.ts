import { ref, computed } from 'vue';

// 전역 상태로 관리하여 다른 컴포넌트에서도 같은 시간을 보게 함
const timeLeft = ref(0);
let timerInterval: ReturnType<typeof setInterval> | null = null;

export function useTimer() {
  /**
   * 타이머 시작 함수
   * @param seconds 시작할 초 단위 시간 (예: 10분 = 600)
   */
  const startTimer = (seconds: number) => { // 여기서 :number 타입을 지정해줘야 합니다.
    if (timerInterval) clearInterval(timerInterval);
    
    timeLeft.value = seconds;

    timerInterval = setInterval(() => {
      if (timeLeft.value > 0) {
        timeLeft.value--;
      } else {
        stopTimer();
      }
    }, 1000);
  };

  const stopTimer = () => {
    if (timerInterval) {
      clearInterval(timerInterval);
      timerInterval = null;
    }
  };

  const formattedTime = computed(() => {
    const m = String(Math.floor(timeLeft.value / 60)).padStart(2, '0');
    const s = String(timeLeft.value % 60).padStart(2, '0');
    return `${m}:${s}`;
  });

  return {
    timeLeft,
    formattedTime,
    startTimer,
    stopTimer
  };
}
<script setup lang="ts">
import { ref, computed } from 'vue';

interface Props {
  inviteCode: string;
}

const props = withDefaults(defineProps<Props>(), {
  inviteCode: 'CODE-ERROR'
});

const emit = defineEmits(['close', 'share']);

const selectedPeriod = ref('7');
const periods = [
  { label: '1일', value: '1' },
  { label: '7일', value: '7' },
  { label: '30일', value: '30' },
  { label: '무제한', value: '0' },
];

/**
 * 드롭다운 메뉴 안의 날짜가 각각 다르게 나오도록 함수로 수정
 */
const calculateDate = (days: string) => {
  if (days === '0') return '만료 기한 없음';
  // 현재 시점(2026년 1월 20일) 기준으로 계산
  const targetDate = new Date(new Date().getTime() + (parseInt(days) * 24 * 60 * 60 * 1000));
  return `${targetDate.getFullYear()}년 ${targetDate.getMonth() + 1}월 ${targetDate.getDate()}일까지`;
};

const copyToClipboard = async () => {
  await navigator.clipboard.writeText(props.inviteCode);
  alert('초대코드가 복사되었습니다.');
};
</script>

<template>
  <div class="modal-content-wrapper">
    <div class="success-box">
      <div class="success-header">
        <span class="material-symbols-outlined">check_circle</span>
        성공적으로 코드가 생성되었습니다.
      </div>
      <div class="code-row">
        <span class="code-text">{{ inviteCode }}</span>
        <button type="button" @click="copyToClipboard" class="copy-btn">
          <span class="material-symbols-outlined">content_copy</span>
          복사
        </button>
      </div>
    </div>

    <div class="input-section">
      <label class="input-label">유효 기간 설정</label>
      <div class="select-container">
        <select v-model="selectedPeriod" class="custom-select">
          <option v-for="p in periods" :key="p.value" :value="p.value">
            {{ p.label }} ({{ calculateDate(p.value) }})
          </option>
        </select>
        <span class="material-symbols-outlined select-arrow">expand_more</span>
      </div>
    </div>

    <div class="info-banner">
      <span class="material-symbols-outlined">info</span>
      <p>
        이 코드를 새로운 팀원에게 전달하면 워크스페이스에 즉시 참여할 수 있습니다. <br />
        <span class="bold-highlight">보안을 위해 권한이 있는 사용자에게만 공유해주세요.</span>
      </p>
    </div>

    <div class="modal-actions">
      <button type="button" @click="$emit('close')" class="cancel-btn">닫기</button>
      <button type="button" @click="$emit('share')" class="share-btn">
        <span class="material-symbols-outlined">share</span>
        공유하기
      </button>
    </div>
  </div>
</template>

<style scoped>
/* 폰트 및 공통 설정 */
.modal-content-wrapper {
  display: flex;
  flex-direction: column;
  font-family: 'Pretendard Variable', 'Noto Sans KR', sans-serif; /* 팀원 코드 스타일 반영 */
}

.font-display {
  font-family: 'Lexend', sans-serif;
}

/* 상단 박스 */
.success-box {
  background-color: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 1rem;
  padding: 1.75rem;
  text-align: center;
  margin-bottom: 1.5rem;
}

.success-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.375rem;
  color: #4F46E5; /* 팀원 Navbar 색상 반영 */
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 1rem;
}

.code-text {
  font-size: 1.875rem;
  font-weight: 900;
  color: #0F172A;
  letter-spacing: 0.05em;
  font-family: 'Lexend', sans-serif;
}

/* 드롭다운 영역 */
.input-section {
  margin-bottom: 1.5rem;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 0.5rem;
  margin-left: 0.25rem;
}

.select-container {
  position: relative;
}

.custom-select {
  width: 100%;
  padding: 0.875rem 1rem;
  background-color: white;
  border: 1px solid #E2E8F0;
  border-radius: 0.75rem;
  font-size: 14px;
  color: #334155;
  appearance: none;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.custom-select:focus {
  border-color: #4F46E5;
}

.select-arrow {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}

/* 안내 배너 */
.info-banner {
  background-color: rgba(79, 70, 229, 0.05);
  border: 1px solid rgba(79, 70, 229, 0.1);
  border-radius: 1rem;
  padding: 1.25rem;
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.info-banner span {
  color: #4F46E5;
  font-size: 20px;
}

.info-banner p {
  font-size: 12px;
  color: #475569;
  line-height: 1.6;
}

.bold-highlight {
  color: #4F46E5;
  font-weight: 700;
}

/* 버튼 */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.625rem;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 0.5rem;
  font-size: 11px;
  font-weight: 700;
  color: #475569;
  transition: all 0.2s;
}

.copy-btn:hover {
  border-color: #4F46E5;
  background-color: #F8FAFC;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  background-color: #4F46E5;
  color: white;
  border-radius: 0.75rem;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.2);
  transition: all 0.2s;
}

.share-btn:hover {
  background-color: #3730A3;
}

.cancel-btn {
  padding: 0.75rem 1.25rem;
  font-size: 14px;
  font-weight: 700;
  color: #64748B;
  transition: color 0.2s;
}

.cancel-btn:hover {
  color: #0F172A;
}
</style>
<script setup lang="ts">
import { ref, computed } from 'vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';

interface Lecture {
  id: number;
  title: string;
  category: string;
  instructor: string;
  studentCount: number;
  progress: number;
  status: 'ongoing' | 'completed' | 'upcoming';
}

// 1. 샘플 데이터 (각 상태별로 골고루 배치)
const lectures = ref<Lecture[]>([
  { id: 1, title: '파이썬 데이터 분석 실무', category: 'IT/개발', instructor: '김철수 강사', studentCount: 128, progress: 72, status: 'ongoing' },
  { id: 2, title: '디지털 마케팅 전략 2024', category: '마케팅', instructor: '박영희 강사', studentCount: 85, progress: 95, status: 'ongoing' },
  { id: 3, title: 'B2B 영업 제안의 정석', category: '영업', instructor: '최민준 강사', studentCount: 210, progress: 45, status: 'ongoing' },
  { id: 4, title: '생성형 AI 업무 자동화 가이드', category: '기술', instructor: '이지혜 강사', studentCount: 340, progress: 0, status: 'upcoming' },
  { id: 5, title: '신입사원을 위한 비즈니스 매너', category: 'HR', instructor: '정태양 강사', studentCount: 50, progress: 100, status: 'completed' }
]);

// 2. 필터 상태 관리
const activeTab = ref<'all' | 'ongoing' | 'upcoming' | 'completed'>('all');
const selectedCategory = ref('전체');
const searchQuery = ref('');

// 3. 탭 메뉴 구성
const tabs = [
  { id: 'all', label: '전체' },
  { id: 'ongoing', label: '진행' },
  { id: 'upcoming', label: '예정' },
  { id: 'completed', label: '종료' }
] as const;

// 4. 카테고리 목록 (데이터 기반 자동 추출)
const categories = computed(() => {
  const list = lectures.value.map(l => l.category);
  return ['전체', ...new Set(list)];
});

// 5. 핵심: 통합 필터링 로직
const filteredLectures = computed(() => {
  return lectures.value.filter(lecture => {
    const matchTab = activeTab.value === 'all' || lecture.status === activeTab.value;
    const matchCategory = selectedCategory.value === '전체' || lecture.category === selectedCategory.value;
    const matchSearch = lecture.title.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    return matchTab && matchCategory && matchSearch;
  });
});
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between px-1">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 bg-indigo-50 rounded-lg flex items-center justify-center text-indigo-600">
          <span class="text-sm">📊</span>
        </div>
        <h3 class="text-xl font-bold text-slate-800 tracking-tight">강의별 수강현황</h3>
      </div>
      <span class="text-[13px] text-slate-400 font-medium bg-slate-100 px-2 py-1 rounded-md">
        전체 {{ filteredLectures.length }}개
      </span>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="flex border-b border-slate-200 bg-white px-4">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="activeTab = tab.id"
          :class="[
            'py-4 px-4 text-sm font-bold transition-all relative',
            activeTab === tab.id ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
          ]"
        >
          {{ tab.label }}
          <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600"></div>
        </button>
      </div>

      <div class="p-4 flex items-center gap-3 bg-slate-50/30 border-b border-slate-100">
        <select 
          v-model="selectedCategory"
          class="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-600 bg-white outline-none focus:border-indigo-500"
        >
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat === '전체' ? '카테고리 전체' : cat }}</option>
        </select>

        <div class="relative flex-1 max-w-xs">
          <input 
            v-model="searchQuery"
            type="text" 
            placeholder="강의명 검색..." 
            class="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm outline-none focus:border-indigo-500"
          />
          <span class="absolute left-3 top-2.5 text-slate-400 text-xs">🔍</span>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/50 text-slate-500 text-[12px] font-bold uppercase tracking-wider">
              <th class="px-6 py-3">강의명</th>
              <th class="px-6 py-3">강사</th>
              <th class="px-6 py-3">수강생</th>
              <th class="px-6 py-3 w-48">진행률</th>
              <th class="px-6 py-3 text-center">상태</th>
              <th class="px-6 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="lecture in filteredLectures" :key="lecture.id" class="hover:bg-slate-50/80 transition-colors group">
              <td class="px-6 py-4">
                <p class="font-bold text-slate-700 group-hover:text-indigo-600 transition-colors">{{ lecture.title }}</p>
                <p class="text-[11px] text-slate-400 mt-0.5">{{ lecture.category }}</p>
              </td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ lecture.instructor }}</td>
              <td class="px-6 py-4 text-sm text-slate-500">{{ lecture.studentCount }}명</td>
              <td class="px-6 py-4"><ProgressBar :progress="lecture.progress" /></td>
              <td class="px-6 py-4 text-center"><StatusBadge :variant="lecture.status" /></td>
              <td class="px-6 py-4 text-right"><span class="text-slate-300 group-hover:text-indigo-400 cursor-pointer">›</span></td>
            </tr>
            <tr v-if="filteredLectures.length === 0">
              <td colspan="6" class="py-20 text-center">
                <p class="text-slate-400 text-sm">해당 조건에 맞는 강의가 없습니다.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
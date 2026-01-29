<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useAdminCourseStore } from '@/stores/adminCourse';
import ProgressBar from '@/components/common/CommonComponents/ProgressBar.vue';
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';

// 1. Pinia store 연결
const courseStore = useAdminCourseStore();

onMounted(() => {
  courseStore.fetchAdminCourses();
});

// 2. 필터 및 검색 상태
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

// 4. 카테고리 목록 (스토어 데이터를 기반으로 자동 생성)
const categories = computed(() => {
  const list = courseStore.adminCourses.map(c => c.category);
  return ['전체', ...new Set(list)];
});

// 5. 필터링된 강의 목록 (lecture -> course로 명칭 통일)
const filteredCourses = computed(() => {
  return courseStore.adminCourses.filter(course => {
    const matchTab = activeTab.value === 'all' || course.status === activeTab.value;
    const matchCategory = selectedCategory.value === '전체' || course.category === selectedCategory.value;
    const matchSearch = course.title.toLowerCase().includes(searchQuery.value.toLowerCase());
    return matchTab && matchCategory && matchSearch;
  });
});

// 6. 상세 보기 핸들러 (행 클릭 시 호출)
const handleRowClick = async (courseId: any) => {
  const numericId = Number(courseId);
  if (isNaN(numericId)) return;

  // 스토어 액션 실행 (404가 나도 catch에서 데이터를 채워줌)
  await courseStore.fetchCourseDetail(numericId);
};
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
        전체 {{ filteredCourses.length }}개
      </span>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="flex border-b border-slate-200 bg-white px-4">
        <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id" :class="[
          'py-4 px-4 text-sm font-bold transition-all relative',
          activeTab === tab.id ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
        ]">
          {{ tab.label }}
          <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600"></div>
        </button>
      </div>

      <div class="p-4 flex items-center gap-3 bg-slate-50/30 border-b border-slate-100">
        <select v-model="selectedCategory"
          class="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-600 bg-white outline-none focus:border-indigo-500">
          <option v-for="cat in categories" :key="cat" :value="cat">
            {{ cat === '전체' ? '카테고리 전체' : cat }}
          </option>
        </select>

        <div class="relative flex-1 max-w-xs">
          <input v-model="searchQuery" type="text" placeholder="강의명 검색..."
            class="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm outline-none focus:border-indigo-500" />
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
            <tr v-for="course in filteredCourses" :key="course.id" @click="handleRowClick(course.id)"
              class="hover:bg-slate-50/80 transition-colors group cursor-pointer">
              <td class="px-6 py-4">
                <p class="font-bold text-slate-700 group-hover:text-indigo-600 transition-colors">{{ course.title }}</p>
                <p class="text-[11px] text-slate-400 mt-0.5">{{ course.category }}</p>
              </td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ course.instructor }}</td>
              <td class="px-6 py-4 text-sm text-slate-500">{{ course.studentCount }}명</td>
              <td class="px-6 py-4">
                <ProgressBar :progress="course.progress" />
              </td>
              <td class="px-6 py-4 text-center">
                <StatusBadge :variant="course.status" />
              </td>
              <td class="px-6 py-4 text-right">
                <span class="text-slate-300 group-hover:text-indigo-400 transition-colors font-bold">›</span>
              </td>
            </tr>

            <tr v-if="filteredCourses.length === 0">
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
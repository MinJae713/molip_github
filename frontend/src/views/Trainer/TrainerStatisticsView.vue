<template>
  <div class="max-w-7xl mx-auto space-y-8">
    <!-- 상단 breadcrumb -->
    <nav aria-label="Breadcrumb" class="flex text-sm text-slate-500">
      <ol class="flex items-center space-x-2">
        <li class="flex items-center">
          <router-link :to="{ name: 'workspaceTrainer', params: { id: workspaceId } }"
            class="hover:text-indigo-600">홈</router-link>
        </li>
        <li class="flex items-center">
          <span class="material-symbols-outlined !text-[1rem] leading-none text-slate-400">chevron_right</span>
        </li>
        <li class="font-medium text-slate-800 flex items-center">강의 통계</li>
      </ol>
    </nav>

    <!-- 제목 + 코스 선택 영역 -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl sm:text-3xl font-bold text-slate-900">강의 성과 리포트</h1>
        <p class="text-slate-500 text-sm mt-1">강의별 학습 성취도와 참여 현황을 분석합니다.</p>
      </div>
      <!-- 코스 선택기 -->
      <div class="flex items-center gap-3 bg-white p-1 pr-2 rounded-lg border border-slate-200 shadow-sm">
        <div class="pl-3 text-xs font-bold text-slate-400 uppercase tracking-wide">Course</div>
        <select v-model="selectedCourseId"
          class="text-sm font-bold text-slate-700 bg-transparent border-none focus:ring-0 cursor-pointer py-1 pr-8">
          <option value="" disabled>강의 선택</option>
          <option v-for="course in trainerCourseStore.managedCourses" :key="course.courseId" :value="course.courseId">
            {{ course.title }}
          </option>
        </select>
      </div>
    </div>

    <!-- 로딩 중일 때 (Skeleton UI) -->
    <template v-if="isLoading">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div v-for="n in 3" :key="n"
          class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm animate-pulse h-32"></div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm animate-pulse h-80"></div>
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm animate-pulse mt-6 h-64"></div>
    </template>

    <!-- 통계 데이터가 있는 경우 -->
    <template v-else-if="stats">
      <!-- 통계 카드 그리드 (3열) -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <TrainerStatCard :data="cardAvgAttendance" />
        <TrainerStatCard :data="cardAvgConcentrate" />
        <TrainerStatCard :data="cardAvgQuiz" />
      </div>

      <!-- 차트 영역 (1열 - 집중도 트렌드만 남김) -->
      <div class="grid grid-cols-1 gap-6">
        <!-- 세션별 집중도 트렌드 -->
        <TrainerSessionTrendChart :stats="stats.sessionAnalysis" title="강의 평균 집중도 변화"
          description="전체 수강생의 세션별 평균 집중도 추이" />
      </div>

      <!-- 상세 리포트 테이블 영역 -->
      <div class="mt-6">
        <TrainerStudentReportTable :students="stats.courseLearnerAnalysis" />
      </div>
    </template>

    <!-- 데이터가 없는 경우 (Empty State) -->
    <div v-else
      class="flex flex-col items-center justify-center py-32 bg-slate-50 border border-dashed border-slate-200 rounded-xl text-center">
      <div class="bg-white p-4 rounded-full shadow-sm mb-4">
        <span class="material-symbols-outlined text-4xl text-slate-300">bar_chart</span>
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-1">표시할 통계가 없습니다</h3>
      <p class="text-slate-500 text-sm max-w-md">강의를 선택하거나, 아직 데이터가 집계되지 않았습니다.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useTrainerCourseStore } from '@/stores/trainerCourse';
import { storeToRefs } from 'pinia';

// Components
import TrainerStatCard from '@/components/Trainer/TrainerStatisticsView/TrainerStatCard.vue';
import TrainerSessionTrendChart from '@/components/Trainer/TrainerStatisticsView/TrainerSessionTrendChart.vue';
import TrainerStudentReportTable from '@/components/Trainer/TrainerStatisticsView/TrainerStudentReportTable.vue';

const route = useRoute();
const trainerCourseStore = useTrainerCourseStore();
const { courseStatistics: stats, isLoading } = storeToRefs(trainerCourseStore);

const workspaceId = computed(() => route.params.id);
const selectedCourseId = ref<string | number>('');

// 카드 데이터 매핑 (Computed)
const cardAvgAttendance = computed(() => ({
  icon: 'groups',
  title: '평균 출석률',
  unit: '%',
  value: stats.value?.avgAttandance ?? 0,
  iconBgClass: 'bg-emerald-50',
  iconColorClass: 'text-emerald-600'
}));

const cardAvgConcentrate = computed(() => ({
  icon: 'psychology',
  title: '평균 집중도',
  unit: '%',
  value: stats.value?.avgConcentrate ?? 0,
  iconBgClass: 'bg-purple-50',
  iconColorClass: 'text-purple-600'
}));

const cardAvgQuiz = computed(() => ({
  icon: 'assignment_turned_in',
  title: '평균 퀴즈 점수',
  unit: '점',
  value: stats.value?.avgQuizCorrect ?? 0,
  iconBgClass: 'bg-indigo-50',
  iconColorClass: 'text-indigo-600'
}));

// 초기 로딩 및 데이터 페칭
onMounted(async () => {
  if (workspaceId.value) {
    // 1. 내가 관리하는 코스 목록 로드
    if (trainerCourseStore.managedCourses.length === 0) {
      await trainerCourseStore.fetchMyCourses(workspaceId.value as string);
    }

    // 2. 초기 코스 선택 (첫 번째 코스)
    if (trainerCourseStore.managedCourses.length > 0) {
      selectedCourseId.value = trainerCourseStore.managedCourses[0].courseId;
    }
  }
});

// 코스 선택 변경 시 통계 데이터 로드
watch(selectedCourseId, async (newId) => {
  if (newId) {
    await trainerCourseStore.fetchCourseStats(newId);
  } else {
    trainerCourseStore.courseStatistics = null;
  }
});
</script>

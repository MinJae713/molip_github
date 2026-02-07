<template>
    <div
        class="group bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-card hover:border-indigo-200 transition-all duration-300 flex flex-col h-full relative overflow-hidden">
        <div class="h-1.5 w-full bg-linear-to-r from-indigo-500 to-cyan-400"></div>
        <div class="p-6 flex-1 flex flex-col">
            <div class="flex justify-between items-start mb-8">
                <span :class="[
                    'px-2.5 py-1 text-[11px] font-bold rounded-full border uppercase tracking-wide',
                    displayCourse('isMandatory')
                        ? 'bg-indigo-50 text-indigo-600 border-indigo-100'
                        : 'bg-slate-100 text-slate-600 border-slate-200'
                ]">
                    {{ displayCourse('category') }}
                </span>
                
                <button @click.prevent.stop="toggleLike" class="transition-colors p-1 group/like"
                    :class="isHeartFilled ? 'text-red-500 hover:text-red-600' : 'text-slate-300 hover:text-indigo-500'"
                    title="찜하기">
                    <span
                        class="material-symbols-outlined text-2xl transition-transform active:scale-95 group-hover/like:scale-110"
                        :style="{ 'font-variation-settings': `'FILL' ${isHeartFilled ? 1 : 0}` } as any">
                        favorite
                    </span>
                </button>
            </div>
            <div class="mb-2">
                <h3 class="text-xl font-bold text-slate-900 leading-snug group-hover:text-indigo-600 transition-colors">
                    {{ displayCourse('title') }}
                </h3>
            </div>
            <p class="text-sm text-slate-500 line-clamp-2 mb-6">{{ displayCourse('description') }}</p>
            <div class="mt-auto">
                <div class="mb-5">
                    <div class="flex justify-between items-end mb-2">
                        <span class="text-xs font-bold text-slate-700">진도율</span>
                        <span class="text-lg font-bold text-indigo-600">{{ progressRate }}%</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                        <div class="bg-indigo-600 h-full rounded-full"
                            :style="{ width: `${progressRate}%` } as any">
                        </div>
                    </div>
                    <div class="flex justify-between mt-2 text-xs text-slate-500">
                        <span>{{ displayCourse('currentLessons') || 0 }}/{{ displayCourse('totalLessons') || 0 }} 세션 완료</span>
                        <span v-if="displayCourse('duration')">{{ displayCourse('duration') }}</span>
                        <span v-else-if="displayCourse('dDay')">{{ getDDayString(displayCourse('dDay')) }}</span>
                    </div>
                </div>

                <button v-if="courseStatus === '수강중'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-indigo-600 text-white border border-indigo-600 hover:bg-indigo-700 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2"
                    @click.prevent="$emit('enter-course', displayCourse('courseId'))">
                    강의실 입장
                    <span class="material-symbols-outlined text-[18px]">login</span>
                </button>

                <button v-else-if="courseStatus === 'FINISH' || displayCourse('status') === 'FINISH'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-slate-700 text-white border border-slate-800 hover:bg-slate-900 transition-all flex items-center justify-center gap-2"
                    @click.prevent="$emit('enter-course', displayCourse('courseId'))">
                    복습하기
                    <span class="material-symbols-outlined text-[18px]">history</span>
                </button>

                <button v-else-if="courseStatus === '승인대기'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-amber-50 text-amber-600 border border-amber-200 cursor-not-allowed flex items-center justify-center gap-2"
                    disabled>
                    승인 대기 중
                    <span class="material-symbols-outlined text-[18px]">hourglass_empty</span>
                </button>

                <button v-else-if="courseStatus === '신청가능'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-indigo-50 text-indigo-600 border border-indigo-100 hover:bg-indigo-100 transition-all flex items-center justify-center gap-2"
                    :disabled="isSubmitting"
                    @click.prevent.stop="handleEnrollClick">
                     {{ isSubmitting ? '처리 중...' : '수강 신청하기' }}
                    <span v-if="!isSubmitting" class="material-symbols-outlined text-[18px]">how_to_reg</span>
                    <span v-else class="material-symbols-outlined text-[18px] animate-spin">sync</span>
                </button>

                <button v-else
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-gray-100 text-gray-500 border border-gray-200 cursor-not-allowed flex items-center justify-center gap-2"
                    disabled>
                    강의 준비 중
                    <span class="material-symbols-outlined text-[18px]">pending</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import type { LearnerCourse } from '@/types';
import { useLearnerCourseStore } from '@/stores/learnerCourse';

interface Props {
    course: LearnerCourse;
    workspaceId: string | number;
}
const props = defineProps<Props>();
const emit = defineEmits<{
    (e: 'enter-course', courseId: string | number): void;
    (e: 'enroll-course', courseId: string | number): void;
}>();

const courseStore = useLearnerCourseStore();
const courseDetail = ref<any>(null);

// [추가] 상세 상태값 도출
const extendedCourseData = computed(() => {
    // 1. props.course에 이미 applyStatus가 있으면 그대로 사용
    if ((props.course as any).applyStatus) return props.course;

    // 2. ID로 찾기
    const courseId = (props.course as any).courseId;
    if (courseId) {
        const found = courseStore.workspaceCourses.find(c => String(c.courseId) === String(courseId));
        if (found) return found;
    }
    
    // 3. ID가 없으면 '제목'으로 찾기 (Zzim API 대응)
    const currentTitle = (props.course as any).title;
    if (currentTitle) {
        const found = courseStore.workspaceCourses.find(c => c.title === currentTitle);
        if (found) return found;
    }

    return props.course;
});

const courseStatus = computed(() => {
    // 수료증 로직 우선
    if ((props.course as any).courseCertificatedId) return 'FINISH';

    const status = (extendedCourseData.value as any).applyStatus;
    // 1. API 명확한 상태
    if (status === 'APPROVE' || status === 'APPROVED') return '수강중';
    if (status === 'PENDING') return '승인대기';
    
    // 2. 상태가 불분명할 경우, 내 수강 목록에 있는지 확인
    const currentId = (props.course as any).courseId || (extendedCourseData.value as any).courseId;
    if (currentId) {
        const isEnrolled = courseStore.myEnrolledCourses.some(c => String(c.courseId) === String(currentId));
        if (isEnrolled) return '수강중';
    }

    // 3. 그 외는 신청 가능 상태
    return '신청가능';
});

// [수정] 찜 목록 데이터(zzimId)에서 제목을 비교하여 찜 상태 판별
const isHeartFilled = computed(() => {
    if ((props.course as any).zzimId) return true;
    const currentTitle = displayCourse('title');
    return courseStore.myLikedCourses.some(
        (liked: any) => liked.title === currentTitle
    );
});

const progressRate = computed(() => {
    // [추가] 수료증 ID가 있으면 100%로 표시
    if ((props.course as any).courseCertificatedId) return 100;

    const current = displayCourse('currentLessons') || 0;
    const total = displayCourse('totalLessons') || 0;
    return total > 0 ? Math.round((current / total) * 100) : 0;
});

// 버튼 처리를 위한 loading
const isSubmitting = ref(false);

const handleEnrollClick = async () => {
    if (isSubmitting.value) return; 
    isSubmitting.value = true;
    try {
        // ID 추출 (props 없으면 extendedData에서)
        const id = (props.course as any).courseId || (extendedCourseData.value as any).courseId;
        if(id) await emit('enroll-course', id); 
        else console.warn("Enroll failed: No course ID found");
    } finally {
        isSubmitting.value = false;
    }
};

onMounted(async () => {
    if (props.course.courseId) {
        const detailedData = await courseStore.fetchCourseInfo(props.course.courseId);
        if (detailedData) {
            courseDetail.value = detailedData;
        }
    }
});

// [수정] 찜 목록에서 해제 시 ID를 찾지 못하는 문제 보완
const toggleLike = async () => {
    // 1. 일반 코스 ID를 먼저 찾고, 없으면 상세 데이터에서 찾음
    let targetCourseId = props.course.courseId || courseDetail.value?.courseId;

    // 2. 만약 찜 목록 탭(zzimId만 있는 상태)이라면 제목이 같은 수강 신청 목록에서 courseId를 매칭
    if (!targetCourseId) {
        const currentTitle = displayCourse('title');
        // 수강 신청 목록에서 찾기
        let match = courseStore.myEnrolledCourses.find(c => c.title === currentTitle);
        
        // 없으면 전체 워크스페이스 목록에서 찾기 (수강 안 한 찜 강의 대응)
        if (!match) {
            match = courseStore.workspaceCourses.find(c => c.title === currentTitle);
        }

        if (match) targetCourseId = match.courseId;
    }

    const currentStatus = isHeartFilled.value;

    if (targetCourseId && props.workspaceId) {
        try {
            await courseStore.toggleCourseLike(
                props.workspaceId,
                targetCourseId,
                currentStatus
            );
            // 3. 성공 후 찜 목록 전체를 새로고침하여 UI 반영
            await courseStore.fetchLikeCourses(props.workspaceId);
        } catch (error) {
            console.error("찜 처리 중 에러 발생:", error);
        }
    } else {
        console.warn("대상 코스 ID를 찾을 수 없습니다.");
    }
};

// [추가] D-Day 포맷팅
const getDDayString = (dateStr: string) => {
    if (!dateStr) return '';
    
    const target = new Date(dateStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    target.setHours(0, 0, 0, 0);
    
    const diff = target.getTime() - today.getTime();
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) return '오늘 개강';
    if (days > 0) return `개강 D-${days}`;
    return `개강 ${Math.abs(days) + 1}일차`;
};

const displayCourse = (field: string) => {
    // 1. 상세 정보 우선
    if (courseDetail.value && courseDetail.value[field] !== undefined) {
        return courseDetail.value[field];
    }
    
    // 2. 확장된 정보 (workspaceCourses 매칭 결과)
    if ((extendedCourseData.value as any)[field] !== undefined) {
        return (extendedCourseData.value as any)[field];
    }

    // 3. 기본 props
    const val = (props.course as any)[field];
    
    // [맵핑] courseTime -> totalLessons (ZzimCourse 대응)
    if (field === 'totalLessons' && val === undefined && (props.course as any).courseTime !== undefined) {
        return (props.course as any).courseTime;
    }

    // 수료 로직 대응
    if (field === 'status') {
         if ((props.course as any).courseCertificatedId) return 'FINISH';
         if (!val) return undefined;
    }
    
    return val;
};

</script>
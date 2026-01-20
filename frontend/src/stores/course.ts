import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface Course {
  id: string | number;
  title: string;
  description: string;
  tag?: string;
  theme: 'indigo' | 'blue' | 'slate'; // UI 표현용
  category: string; // 서버 데이터용
  progress: number;
  currentLessons: number;
  totalLessons: number;
}

// [사고의 흐름] 카테고리별 테마를 여기서 관리합니다.
const CATEGORY_THEME_MAP: Record<string, 'indigo' | 'blue' | 'slate'> = {
  'ai': 'indigo',
  'data': 'blue',
  'security': 'slate',
  'default': 'indigo'
};

export const useCourseStore = defineStore('course', () => {
  const myCourses = ref<Course[]>([]);
  const isLoading = ref(false);

  const fetchMyCourses = async () => {
    isLoading.value = true;
    try {
      const response = await fetch('http://localhost:3000/my_courses');
      const data = await response.json();

      myCourses.value = data.map((course: any) => {
        // [핵심] 수료 여부(100%)면 무조건 slate로 보여주고, 아니면 카테고리 테마 적용
        const isCompleted = course.progress_rate === 100;
        const mappedTheme = isCompleted 
          ? 'slate' 
          : (CATEGORY_THEME_MAP[course.category] || CATEGORY_THEME_MAP.default);

        return {
          id: course.id,
          title: course.title,
          description: course.description || '',
          tag: course.tag,
          category: course.category,
          theme: mappedTheme, // 프론트에서 결정된 테마
          progress: course.progress_rate,
          currentLessons: course.current_lessons,
          totalLessons: course.total_lessons
        };
      });
    } catch (error) {
      console.error('강의 데이터 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  };

  return { myCourses, isLoading, fetchMyCourses };
});
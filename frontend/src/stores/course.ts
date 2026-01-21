import { defineStore } from 'pinia';
import { ref } from 'vue';

import api from '@/api';
import type { Course } from '@/types';

// 카테고리별 테마를 여기서 관리
const CATEGORY_THEME_MAP: Record<string, 'indigo' | 'blue' | 'slate' | 'amber'> = {
  'ai': 'indigo',
  'data': 'blue',
  'security': 'slate',
  'marketing': 'amber',
  'default': 'indigo'
};

export const useCourseStore = defineStore('course', () => {
  const myEnrolledCourses = ref<Course[]>([]);
  const workspaceCourses = ref<Course[]>([]);
  const isLoading = ref(false);

  // API 응답을 Course 타입으로 변환하는 함수
  const mapCourseData = (data: any[]): Course[] => {
    return data.map((course: any) => ({
      id: course.id,
      workspace_id: course.workspace_id,
      title: course.title,
      description: course.description || '',
      tag: course.tag,
      tag_color: course.tag_color,
      category: course.category,
      theme: 'indigo' as const, // 서버에서 결정하거나 기본값 사용
      progress: course.progress_rate || 0,
      currentLessons: course.current_lessons || 0,
      totalLessons: course.total_lessons || 0,
      is_mandatory: course.is_mandatory,
      d_day: course.d_day,
      is_liked: course.is_liked,
      sub_tag: course.sub_tag,
      duration: course.duration,
      status: course.status,
      request_date: course.request_date
    }));
  };

  // 현재 워크스페이스에서 제공하는 모든 강의 목록 가져오기
  async function fetchCoursesByWorkspace(workspaceId: string | number) {
    isLoading.value = true;
    try {
      const response = await api.get(`/workspaces/${workspaceId}/courses`);
      // 프론트엔드에서 필터링 (routes.json의 쿼리 파라미터 매핑이 작동하지 않음)
      const allCourses = mapCourseData(response.data);
      workspaceCourses.value = allCourses.filter(
        (c: any) => String(c.workspace_id) === String(workspaceId)
      );
    } catch (error) {
      console.error('워크스페이스 강의 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  }
  // 내가 참가한 강의 목록 조회
  async function fetchMyEnrolledCourses() {
      isLoading.value = true;
      try {
          const response = await api.get('/members/me/courses');
          myEnrolledCourses.value = mapCourseData(response.data);
      } catch (error) {
          console.error('참가 강의 목록 로드 실패:', error);
      } finally {
          isLoading.value = false;
      }
  }
  // 강의 찜하기 및 취소 (별도 찜 테이블 관리)
  // - isCurrentlyLiked: 현재 좋아요 상태 (UI에서 넘겨줌)
  async function toggleCourseLike(courseId: number, isCurrentlyLiked: boolean) {
      try {
          if (isCurrentlyLiked) {
              // 찜 취소: courseId로 레코드를 찾아서 삭제
              const response = await api.get(`/course_likes?courseId=${courseId}`);
              const likeRecord = response.data[0];
              if (likeRecord) {
                  await api.delete(`/members/me/course-likes/${likeRecord.id}`);
              }
          } else {
              // 찜 등록: POST 호출 (찜 테이블에 레코드 추가)
              await api.post('/members/me/course-likes', { courseId: String(courseId) });
          }
          // [UI 동기화] 서버 DB는 바뀌었으니, 현재 메모리에 있는 리스트들의 하트 상태도 반전시켜줍니다.
          const updateState = (list: Course[]) => {
              const target = list.find(c => Number(c.id) === courseId);
              if (target) target.is_liked = !isCurrentlyLiked;
          };
          updateState(workspaceCourses.value);
          updateState(myEnrolledCourses.value);
      } catch (error) {
          console.error('찜 기능 처리 실패:', error);
      }
  }

  // 강의 수강 신청 (별도 수강신청 테이블 관리)
  async function enrollCourse(courseId: string | number, memberId: string | number) {
      try {
          const payload = {
              courseId: String(courseId),
              memberId: String(memberId),
              status: 1, // 1: 승인 대기, 2: 승인 완료
              applied_at: new Date().toISOString()
          };
          
          await api.post(`/courses/${courseId}/enroll`, payload);
          
          // 신청 후 강의 목록 갱신 (상태가 pending_approval로 변경되도록)
          // 실제로는 서버에서 status를 업데이트해서 보내줘야 함
          return true;
      } catch (error) {
          console.error('수강 신청 실패:', error);
          return false;
      }
  }

  const fetchCourseSessions = async (courseId: string | number) => {
      try {
          const response = await api.get(`/courses/${courseId}/sessions`);
          return response.data; // Return sessions directly
      } catch (error) {
          console.error('세션 데이터 로드 실패:', error);
          return [];
      }
  };


  return { 
    myEnrolledCourses, 
    workspaceCourses, 
    isLoading, 
    fetchMyEnrolledCourses, 
    fetchCoursesByWorkspace, 
    toggleCourseLike,
    enrollCourse,
    fetchCourseSessions
  };
});
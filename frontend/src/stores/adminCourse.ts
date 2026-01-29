import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '@/api/index.ts';
import dayjs from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';

dayjs.extend(isBetween);

// --- Interfaces ---
export type SessionStatus = '완료' | '라이브' | '대기';

export interface Session {
  id: number;
  title: string;
  startTime: string;
  endTime: string;
  participationRate: number;
  status: SessionStatus;
}

export interface InstructorDetail {
  name: string;
  role: string;
  email: string;
  phone: string;
  profileImage?: string;
}

export interface AdminCourse {
  id: number;
  title: string;
  category: string;
  instructor: string;
  studentCount: number;
  progress: number;
  status: 'ongoing' | 'completed' | 'upcoming';
}

export interface CourseDetail extends AdminCourse {
  averageProgress: number;
  targetCompletionDate: string;
  instructorDetail: InstructorDetail | null;
  sessions: Session[];
}

export const useAdminCourseStore = defineStore('adminCourse', () => {
  const adminCourses = ref<AdminCourse[]>([]);
  const selectedCourse = ref<CourseDetail | null>(null);
  const isLoading = ref(false);

  const getSessionStatus = (start: string, end: string): SessionStatus => {
    if (!start || !end) return '대기';
    const now = dayjs();
    const startTime = dayjs(start);
    const endTime = dayjs(end);
    if (now.isAfter(endTime)) return '완료';
    if (now.isBetween(startTime, endTime)) return '라이브';
    return '대기';
  };

  const fetchAdminCourses = async () => {
    isLoading.value = true;
    try {
      const { data } = await api.get('/admin_courses');
      adminCourses.value = data.map((c: any) => ({
        id: Number(c.id),
        title: c.title,
        category: c.category,
        instructor: c.instructor,
        studentCount: c.student_count || 0,
        progress: c.progress || 0,
        status: c.status
      }));
    } catch (err) {
      console.error('강의 목록 로드 실패:', err);
    } finally {
      isLoading.value = false;
    }
  };

  const fetchCourseDetail = async (courseId: number) => {
    isLoading.value = true;
    
    // 💡 중요: 호출 즉시 이전 상태를 비워서 로딩 UI가 나오게 함
    selectedCourse.value = null; 

    try {
      const { data } = await api.get(`/course_details/${courseId}`);

      selectedCourse.value = {
        id: Number(data.id),
        title: data.title || '정보 없음',
        category: data.category || '기타',
        instructor: data.instructor || '미지정',
        studentCount: data.student_count || 0,
        progress: data.progress || 0,
        status: data.status || 'upcoming',
        averageProgress: data.average_progress || data.progress || 0,
        targetCompletionDate: data.target_completion_date || data.end_date || '-',
        instructorDetail: data.instructor_detail ? {
          name: data.instructor_detail.name || data.instructor || '미지정',
          role: data.instructor_detail.role || '교육 담당',
          email: data.instructor_detail.email || '-',
          phone: data.instructor_detail.phone || '-',
          profileImage: data.instructor_detail.profileImage || ''
        } : null,
        sessions: (data.curriculum || data.sessions || []).map((s: any) => ({
          id: s.id,
          title: s.title,
          startTime: s.startTime,
          endTime: s.endTime,
          participationRate: s.participationRate || 0,
          status: getSessionStatus(s.startTime, s.endTime)
        }))
      };
    } catch (err) {
      console.warn(`데이터 없음(404) 대응: ID ${courseId}`);
      // 💡 핵심: 에러 시에도 'selectedCourse'에 객체를 넣어줘야 모달의 v-if가 작동함
      selectedCourse.value = {
        id: courseId,
        title: '강의 정보를 불러올 수 없습니다',
        category: '-',
        instructor: '-',
        studentCount: 0,
        progress: 0,
        status: 'upcoming',
        averageProgress: 0,
        targetCompletionDate: '-',
        instructorDetail: null,
        sessions: []
      };
    } finally {
      isLoading.value = false;
    }
  };

  const clearSelection = () => { selectedCourse.value = null; };

  return { adminCourses, selectedCourse, isLoading, fetchAdminCourses, fetchCourseDetail, clearSelection };
});
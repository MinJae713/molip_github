import { createRouter, createWebHistory } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspace' // 스토어 임포트
import MainLayout from '@/layouts/MainLayout.vue'
import TestView from '@/views/TestView.vue'
import WorkspacesView from '@/views/WorkspacesView.vue'
import MypageView from '@/views/MypageView.vue'
import WorkspaceDetailView from '@/views/WorkspaceDetailView.vue'
import MyCoursesView from '@/views/MyCoursesView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 2. 부모 라우트: 모든 페이지를 감싸는 레이아웃입니다.
      path: '/',
      component: MainLayout,
      // 3. 자식 라우트: MainLayout 내부의 <RouterView />에 렌더링될 페이지들입니다.
      children: [
        {
          path: '', // 주소가 '/' 일 때 (기본 페이지)
          name: 'home',
          component: TestView
        }
      ]
    },
    {
      path: '/workspaces',
      component: OnlyNavbarLayout,
      children: [
        {
          path: '',
          name: 'workspaces',
          component: WorkspacesView
        }
      ]
    },
    {
      path: '/mypage',
      component: MypageLayout,
      children: [
        {
          path: '',
          name: 'mypage',
          component: MypageView
        }
      ]
    },
    {
      path: '/workspaceDetail/:id',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'workspaceDetail',
          component: WorkspaceDetailView
        },
        {
          path: 'myCourses',
          name: 'myCourses',
          component: MyCoursesView
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  // workspaceDetail 페이지나 그 하위 경로로 진입할 때만 로직 실행
  if (to.name === 'workspaceDetail' || to.path.includes('/workspaceDetail/')) {
    const workspaceStore = useWorkspaceStore()
    const targetId = to.params.id as string

    // 목록이 비어있다면(새로고침 등) 먼저 데이터를 불러옴
    if (workspaceStore.myWorkspaces.length === 0) {
      await workspaceStore.fetchMyWorkspaces()
    }

    // 권한 체크 (스토어에 만든 isAccessible 또는 find 로직 활용)
    const hasAccess = workspaceStore.isAccessible(targetId)
    if (!hasAccess) {
      alert('해당 워크스페이스에 대한 접근 권한이 없습니다.')
      return next({ name: 'mypage' }) // 권한 없으면 마이페이지로 리다이렉트
    }
    // 권한이 있다면 현재 워크스페이스 정보 동기화 후 진입
    await workspaceStore.setCurrentWorkspaceById(targetId)
  }
  
  next() // 그 외의 페이지는 정상 진입
})

export default router
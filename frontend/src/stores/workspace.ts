import { ref, watch } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api'

interface Workspace {
    id: string | number;
    name: string;
    logo_url?: string;
    role?: string;
    status?: number;
}

export const useWorkspaceStore = defineStore('workspace', () => {
    // 초기화: 로컬 스토리지 확인 (있을 때만 가져옴)
    const storedMyWorkspaces = localStorage.getItem('my_workspaces')
    const storedCurrentWorkspace = localStorage.getItem('current_workspace')

    const myWorkspaces = ref<Workspace[]>(storedMyWorkspaces ? JSON.parse(storedMyWorkspaces) : [])
    
    // 초기에는 null 또는 빈 객체로 설정하여 선택되지 않음을 명시
    const currentWorkspace = ref<Workspace | null>(
        storedCurrentWorkspace ? JSON.parse(storedCurrentWorkspace) : null
    )

    const isLoaded = ref(!!storedMyWorkspaces)

    //전체 워크스페이스 목록만 가져오기
    async function fetchMyWorkspaces(forceUpdate = false) {
        if (isLoaded.value && !forceUpdate) return

        try {
            const response = await api.get('/my_workspaces')
            const allMyWorkspaces = response.data as Workspace[]
            // 대기(1) 또는 참여중(2)인 워크스페이스만 필터링
            const filteredWorkspaces = allMyWorkspaces.filter(ws => ws.status && ws.status > 0)
            
            myWorkspaces.value = filteredWorkspaces
            localStorage.setItem('my_workspaces', JSON.stringify(filteredWorkspaces))
            isLoaded.value = true
        } catch (error) {
            console.error('Failed to fetch workspace list:', error)
        }
    }

    // 특정 ID로 현재 워크스페이스 수동 설정 (상세 페이지용)
    async function setCurrentWorkspaceById(id: string | number) {
        // 이미 목록(myWorkspaces)에 데이터가 있는지 확인
        const found = myWorkspaces.value.find(ws => String(ws.id) === String(id))
        
        if (found) {
            currentWorkspace.value = found
        } else {
            // 목록에 없다면 서버에 단일 정보 요청 (새로고침 대응)
            try {
                const response = await api.get(`/workspaces/${id}`)
                currentWorkspace.value = response.data
            } catch (error) {
                console.error('Failed to fetch detail workspace info:', error)
                currentWorkspace.value = null // 찾지 못하면 초기화
            }
        }
    }

    // 상태 감시 및 자동 저장
    watch(currentWorkspace, (newVal) => {
        if (newVal) {
            localStorage.setItem('current_workspace', JSON.stringify(newVal))
        } else {
            localStorage.removeItem('current_workspace')
        }
    }, { deep: true })

    return { 
        currentWorkspace, 
        myWorkspaces, 
        fetchMyWorkspaces, 
        setCurrentWorkspaceById 
    }
})
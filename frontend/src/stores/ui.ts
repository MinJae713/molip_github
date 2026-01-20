import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api'

interface MenuItem {
    id: string;
    name: string;
    icon: string;
    path: string;
}

export const useUiStore = defineStore('ui', () => {
    const studyMenus = ref<MenuItem[]>([])
    const communityMenus = ref<MenuItem[]>([])
    const isLoaded = ref(false)

    async function fetchSidebarMenus() {
        if (isLoaded.value) return

        try {
            const response = await api.get('/sidebar_menus')
            studyMenus.value = response.data.study
            communityMenus.value = response.data.community
            isLoaded.value = true
        } catch (error) {
            console.error('Failed to fetch sidebar menus:', error)
        }
    }

    return { studyMenus, communityMenus, fetchSidebarMenus }
})

import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const user = ref({
    name: '',
    email: '',
    profileImage: ''
  })

  // 데이터 로딩 여부 (선택 사항)
  const isLoaded = ref(false)

  async function fetchUserInfo() {
    // 이미 데이터가 있다면 요청하지 않음 (캐싱 효과)
    if (isLoaded.value) return

    try {
      const response = await api.get('/user')
      user.value = response.data
      isLoaded.value = true
    } catch (error) {
      console.error('Failed to fetch user info:', error)
    }
  }

  return { user, fetchUserInfo }
})

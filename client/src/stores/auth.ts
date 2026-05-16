import { defineStore } from 'pinia'
import api from '../utils/api'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref<number | null>(null)
  const username = ref('')
  const role = ref('')
  const isLoading = ref(false)
  const isReady = ref(false)

  const configureAxios = () => {
    if (token.value) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    } else {
      delete api.defaults.headers.common['Authorization']
    }
  }

  const setAuth = (newToken: string, newUserId: number, newUsername: string, newRole?: string) => {
    token.value = newToken
    userId.value = newUserId
    username.value = newUsername
    if (newRole) role.value = newRole
    localStorage.setItem('token', newToken)
    configureAxios()
    isReady.value = true
  }

  const logout = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    role.value = ''
    localStorage.removeItem('token')
    configureAxios()
    isReady.value = true
  }

  const initAuth = async () => {
    const storedToken = localStorage.getItem('token')
    if (!storedToken) {
      isReady.value = true
      return
    }

    token.value = storedToken
    configureAxios()

    try {
      isLoading.value = true
      const res = await api.get('/api/auth/me')
      if (res.data.code === 200 && res.data.data) {
        userId.value = res.data.data.id
        username.value = res.data.data.username
        role.value = res.data.data.role || ''
      }
    } catch (e) {
      console.warn('Token validation failed:', e)
      logout()
    } finally {
      isLoading.value = false
      isReady.value = true
    }
  }

  return { token, userId, username, role, isLoading, isReady, setAuth, logout, initAuth }
})
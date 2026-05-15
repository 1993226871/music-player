import { defineStore } from 'pinia'
import axios from 'axios'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref<number | null>(null)
  const username = ref('')

  const setAuth = (newToken: string, newUserId: number, newUsername: string) => {
    token.value = newToken
    userId.value = newUserId
    username.value = newUsername
    localStorage.setItem('token', newToken)
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }

  const logout = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    localStorage.removeItem('token')
  }

  const initAuth = () => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      token.value = storedToken
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
    }
  }

  return { token, userId, username, setAuth, logout, initAuth }
})
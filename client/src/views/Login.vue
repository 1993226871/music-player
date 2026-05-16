<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <svg viewBox="0 0 24 24" width="40" height="40" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
      </div>
      <h1>登录</h1>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input v-model="username" type="text" placeholder="用户名" required />
        </div>
        <div class="input-group">
          <input v-model="password" type="password" placeholder="密码" required />
        </div>
        <button type="submit" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>
      </form>
      <p class="register-link">
        还没有账号？<router-link to="/register">注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import api from '../utils/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await api.post('/api/auth/login', { username: username.value, password: password.value })
    auth.setAuth(res.data.data.token, res.data.data.userId, res.data.data.username)
    router.push('/')
  } catch (e: any) {
    alert(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-darker) 100%);
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 40px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
}

.logo {
  color: var(--primary);
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

h1 {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 32px;
}

.input-group {
  margin-bottom: 20px;
}

.input-group input {
  width: 100%;
  padding: 14px 16px;
  background: var(--bg-dark);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-primary);
  font-size: 15px;
  transition: var(--transition);
}

.input-group input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(236, 65, 65, 0.2);
}

.input-group input::placeholder {
  color: var(--text-muted);
}

button {
  width: 100%;
  padding: 14px;
  background: #ec4141;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  border: 2px solid #ec4141;
  transition: all 0.2s;
}

button:hover:not(:disabled) {
  background: #c93030;
  border-color: #c93030;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.register-link a {
  color: var(--primary);
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
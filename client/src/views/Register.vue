<template>
  <div class="register-page">
    <h1>注册</h1>
    <form @submit.prevent="handleRegister">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">注册</button>
    </form>
    <p>已有账号？<router-link to="/login">登录</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const router = useRouter()
const auth = useAuthStore()

const handleRegister = async () => {
  const res = await axios.post('http://localhost:8080/api/auth/register', { username: username.value, password: password.value })
  auth.setAuth(res.data.data.token, res.data.data.userId, res.data.data.username)
  router.push('/')
}
</script>

<style scoped>
.register-page { display: flex; flex-direction: column; align-items: center; padding: 50px; }
input { margin: 10px 0; padding: 10px; width: 300px; }
button { padding: 10px 30px; background: #ec4141; color: white; border: none; cursor: pointer; }
</style>
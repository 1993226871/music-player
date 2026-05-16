<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h2>管理员面板</h2>
    </div>

    <div class="admin-content">
      <div class="card">
        <h3>Cookie 管理</h3>
        <div class="cookie-status">
          <div class="status-item">
            <span class="label">Cookie状态:</span>
            <span :class="['value', hasCookie ? 'active' : 'inactive']">
              {{ hasCookie ? '已配置' : '未配置' }}
            </span>
          </div>
          <div class="status-item" v-if="hasCookie">
            <span class="label">Cookie长度:</span>
            <span class="value">{{ cookieLength }} 字符</span>
          </div>
        </div>

        <div class="upload-section">
          <h4>更新 Cookie</h4>
          <p class="upload-tip">步骤：</p>
          <ol class="upload-steps">
            <li>打开浏览器访问 <a href="https://music.163.com" target="_blank">网易云音乐</a></li>
            <li>登录你的VIP账号</li>
            <li>按 F12 打开开发者工具</li>
            <li>在 Network 标签页找到任意请求，查看 Request Headers 中的 Cookie</li>
            <li>复制整个 Cookie 字符串，粘贴到下方文本框</li>
            <li>点击"更新Cookie"按钮</li>
          </ol>

          <textarea
            v-model="cookieInput"
            placeholder="粘贴 Cookie 内容到这里..."
            rows="5"
          ></textarea>

          <button class="btn-primary" @click="updateCookie" :disabled="updating || !cookieInput">
            <span v-if="updating">更新中...</span>
            <span v-else>更新Cookie</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const hasCookie = ref(false)
const cookieLength = ref(0)
const updating = ref(false)
const cookieInput = ref('')

const checkCookieStatus = async () => {
  try {
    const res = await api.get('/api/admin/cookie-status')
    hasCookie.value = res.data.data.hasCookie
    cookieLength.value = res.data.data.cookieLength
  } catch (e) {
    console.error('Failed to check cookie status:', e)
  }
}

const updateCookie = async () => {
  if (!cookieInput.value.trim()) {
    alert('请输入 Cookie 内容')
    return
  }
  updating.value = true
  try {
    const res = await api.post('/api/admin/upload-cookie', {
      cookie: cookieInput.value.trim()
    })
    if (res.data.code === 200) {
      alert('Cookie更新成功！')
      cookieInput.value = ''
      await checkCookieStatus()
    } else {
      alert('更新失败: ' + (res.data.code || '') + ' ' + (res.data.message || '未知错误'))
    }
  } catch (e: any) {
    const msg = e.response?.data?.message || e.message
    alert('更新失败: ' + msg)
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  checkCookieStatus()
})
</script>

<style scoped>
.admin-panel {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.admin-header h2 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
  font-weight: 600;
}

.cookie-status {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
  border: 1px solid #dee2e6;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #dee2e6;
}

.status-item:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  font-size: 14px;
  font-weight: 500;
}

.value.active {
  color: #51cf66;
}

.value.inactive {
  color: #ff6b6b;
}

.upload-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.upload-section h4 {
  font-size: 15px;
  color: #333;
  margin-bottom: 12px;
}

.upload-tip {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.upload-steps {
  font-size: 13px;
  color: #555;
  margin-bottom: 16px;
  padding-left: 20px;
  line-height: 1.8;
}

.upload-steps a {
  color: #ec4141;
}

textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
  font-family: monospace;
  resize: vertical;
  margin-bottom: 16px;
  background: #fafafa;
  color: #333;
}

textarea:focus {
  border-color: #ec4141;
  outline: none;
}

.btn-primary {
  width: 100%;
  padding: 14px 20px;
  background: #ec4141;
  color: #fff;
  border: 2px solid #ec4141;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: block;
}

.btn-primary:hover:not(:disabled) {
  background: #c93030;
  border-color: #c93030;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
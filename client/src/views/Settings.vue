<template>
  <div class="settings-page">
    <h1>设置</h1>

    <div class="settings-section">
      <h3>服务器配置</h3>
      <div class="setting-item">
        <label>服务器地址</label>
        <input
          v-model="serverUrl"
          type="text"
          placeholder="http://your-server-ip:8080"
        />
        <span class="hint">例如: http://119.91.55.249:8080</span>
      </div>
      <button @click="saveServerUrl" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
      <p v-if="saved" class="success">服务器地址已保存！</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { setServerUrl, getServerUrl } from '../utils/api'

const serverUrl = ref('')
const saving = ref(false)
const saved = ref(false)

onMounted(() => {
  serverUrl.value = getServerUrl()
})

const saveServerUrl = async () => {
  if (!serverUrl.value.trim()) return

  saving.value = true
  try {
    setServerUrl(serverUrl.value.trim())
    saved.value = true
    setTimeout(() => { saved.value = false }, 3000)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
  max-width: 500px;
  margin: 0 auto;
}

h1 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.settings-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
}

h3 {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.setting-item {
  margin-bottom: 16px;
}

.setting-item label {
  display: block;
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.setting-item input {
  width: 100%;
  padding: 12px;
  background: var(--bg-dark);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-primary);
  font-size: 14px;
}

.setting-item input:focus {
  border-color: var(--primary);
  outline: none;
}

.hint {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}

button {
  padding: 12px 24px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}

button:hover:not(:disabled) {
  background: #c93030;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success {
  color: #51cf66;
  font-size: 13px;
  margin-top: 12px;
}
</style>
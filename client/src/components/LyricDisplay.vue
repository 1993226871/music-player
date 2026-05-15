<template>
  <div class="lyric-panel" :class="{ open: showLyric }">
    <div class="lyric-content">
      <div v-for="(line, index) in lyricLines" :key="index"
           class="lyric-line"
           :class="{ active: index === currentLineIndex }">
        {{ line.text }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const player = usePlayerStore()
const { lyricLines, currentTime, showLyric } = storeToRefs(player)

const currentLineIndex = computed(() => {
  for (let i = lyricLines.value.length - 1; i >= 0; i--) {
    if (currentTime.value >= lyricLines.value[i].time) return i
  }
  return 0
})
</script>

<style scoped>
.lyric-panel {
  position: fixed;
  right: -400px;
  top: 0;
  bottom: 80px;
  width: 400px;
  background: #fff;
  box-shadow: -2px 0 10px rgba(0,0,0,0.1);
  transition: right 0.3s;
  overflow-y: auto;
}
.lyric-panel.open { right: 0; }
.lyric-content { padding: 20px; }
.lyric-line { padding: 10px 0; font-size: 16px; }
.lyric-line.active { color: #ec4141; font-weight: bold; }
</style>
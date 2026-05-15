<template>
  <div class="player-bar">
    <div class="player-left">
      <img :src="currentSong?.coverUrl || '/default-cover.png'" class="cover" />
      <div class="song-info">
        <div class="song-name">{{ currentSong?.name || '未播放' }}</div>
        <div class="artist">{{ currentSong?.artist || '-' }}</div>
      </div>
    </div>
    <div class="player-center">
      <div class="controls">
        <button @click="playPrev">上一首</button>
        <button @click="togglePlay">{{ isPlaying ? '暂停' : '播放' }}</button>
        <button @click="playNext">下一首</button>
      </div>
      <div class="progress">
        <span>{{ formatTime(currentTime) }}</span>
        <input type="range" :value="currentTime" :max="duration" @input="seek" />
        <span>{{ formatTime(duration) }}</span>
      </div>
    </div>
    <div class="player-right">
      <button @click="showLyric = !showLyric">歌词</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const player = usePlayerStore()
const { currentSong, isPlaying, currentTime, duration, showLyric } = storeToRefs(player)
const { togglePlay, playNext, playPrev, setCurrentTime } = player

const seek = (e: Event) => {
  const target = e.target as HTMLInputElement
  setCurrentTime(Number(target.value))
}

const formatTime = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.player-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: #333;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.player-left { display: flex; align-items: center; }
.cover { width: 50px; height: 50px; }
.song-info { margin-left: 10px; }
.player-center { flex: 1; display: flex; flex-direction: column; align-items: center; }
.controls button { margin: 0 5px; }
.progress { display: flex; align-items: center; gap: 10px; }
</style>
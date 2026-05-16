<template>
  <div class="player-bar">
    <!-- Left: Current Song Info -->
    <div class="player-left">
      <div class="cover-wrapper">
        <img :src="currentSong?.coverUrl || '/default-cover.png'" class="cover" />
        <div class="cover-overlay" v-if="currentSong">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M8 5v14l11-7z"/>
          </svg>
        </div>
      </div>
      <div class="song-info" v-if="currentSong">
        <div class="song-name">{{ currentSong.name }}</div>
        <div class="artist">{{ currentSong.artist }}</div>
      </div>
      <div class="song-info" v-else>
        <div class="song-name">未播放</div>
        <div class="artist">-</div>
      </div>
    </div>

    <!-- Center: Controls -->
    <div class="player-center">
      <div class="controls">
        <button class="control-btn" @click="playPrev" title="上一首">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/>
          </svg>
        </button>
        <button class="control-btn play-btn" @click="togglePlay">
          <svg v-if="!isPlaying" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <path d="M8 5v14l11-7z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
          </svg>
        </button>
        <button class="control-btn" @click="playNext" title="下一首">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/>
          </svg>
        </button>
      </div>

      <!-- Progress Bar -->
      <div class="progress-section">
        <span class="time current">{{ formatTime(currentTime) }}</span>
        <div class="progress-bar" ref="progressBarRef" @click="handleProgressClick" @mousemove="handleProgressHover" @mouseleave="hideHoverTime" @mouseenter="showHoverTime">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
            <div class="progress-hover" :style="{ width: hoverPercent + '%' }" v-if="isHovering"></div>
            <div class="progress-knob" :style="{ left: progressPercent + '%' }" v-if="duration > 0"></div>
          </div>
          <input
            type="range"
            class="progress-input"
            :value="currentTime"
            :max="duration || 100"
            @input="seek"
          />
          <div class="hover-time" :style="{ left: hoverPercent + '%' }" v-if="isHovering && hoverPercent >= 0">{{ formatTime(hoverTime) }}</div>
        </div>
        <span class="time total">{{ formatTime(duration) }}</span>
        <div class="preview-badge" v-if="isPreview">试听</div>
      </div>
    </div>

    <!-- Right: Volume & Extra -->
    <div class="player-right">
      <button class="control-btn" @click="toggleLyric" :class="{ active: showLyric }" title="歌词">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
        </svg>
      </button>
      <div class="volume-control" @mouseenter="showVolumeBar" @mouseleave="hideVolumeBar">
        <button class="control-btn" @click="toggleMute" title="音量">
          <svg v-if="volume > 0" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"/>
          </svg>
        </button>
        <div class="volume-bar" :class="{ visible: isVolumeBarVisible }" @click="handleVolumeClick" @mousemove="handleVolumeHover" @mouseleave="hideVolumeHover">
          <div class="volume-track">
            <div class="volume-fill" :style="{ width: volume * 100 + '%' }"></div>
            <div class="volume-hover" :style="{ width: volumeHoverPercent + '%' }" v-if="isVolumeHovering"></div>
            <div class="volume-knob" :style="{ left: volume * 100 + '%' }" v-if="volume > 0"></div>
          </div>
          <input
            type="range"
            class="volume-input"
            :value="volume"
            max="1"
            step="0.01"
            @input="handleVolumeChange"
          />
        </div>
      </div>
    </div>

    <!-- Hidden Audio Element -->
    <audio ref="audioEl" @timeupdate="onTimeUpdate" @durationchange="onDurationChange" @loadedmetadata="onLoadedMetadata" @ended="onEnded" @error="onError" @canplay="onCanPlay"></audio>

    <!-- Lyric Display -->
    <LyricDisplay v-if="showLyric" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'
import LyricDisplay from './LyricDisplay.vue'

const audioEl = ref<HTMLAudioElement | null>(null)
const showLyric = ref(false)
const isHovering = ref(false)
const hoverPercent = ref(0)
const hoverTime = ref(0)
const isVolumeHovering = ref(false)
const volumeHoverPercent = ref(0)
const isVolumeBarVisible = ref(false)

const player = usePlayerStore()
const { currentSong, isPlaying, currentTime, duration, volume, isPreview } = storeToRefs(player)
const { togglePlay: storeTogglePlay, playNext, playPrev, setCurrentTime, setVolume, setIsPreview } = player

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

// Watch for song changes to load and play
watch(currentSong, async (song) => {
  if (!song || !audioEl.value) return

  // Reset state
  player.setDuration(0)
  player.setCurrentTime(0)

  // Get song URL directly from API (bypasses CDN restrictions via server-side cookie)
  try {
    const res = await fetch(`/api/songs/${song.id}/url`)
    const data = await res.json()
    if (data.data?.url) {
      // Fix URL encoding for + signs
      let url = data.data.url
      if (url.includes('vuutv=') && url.includes('+')) {
        url = url.replace(/\+/g, '%2B')
      }
      audioEl.value.src = url
      audioEl.value.load()
      player.setIsPlaying(true)

      // Show warning if cookie expired (VIP songs will only play 45s preview)
      if (data.data.cookieExpired) {
        console.warn('Cookie expired - VIP songs will only play 45s preview')
        // 可以在这里显示提示给用户
      }

      // Show preview indicator for non-VIP songs
      if (data.data.isPreview) {
        console.log('Playing preview version (45 seconds)')
        setIsPreview(true)
      } else {
        setIsPreview(false)
      }
    }
  } catch (e) {
    console.error('Failed to get song URL:', e)
  }
})

// Watch for playing state
watch(isPlaying, (playing) => {
  if (!audioEl.value || !audioEl.value.src) return
  if (playing) {
    audioEl.value.play().catch(console.error)
  } else {
    audioEl.value.pause()
  }
})

// Watch for volume changes from store
watch(volume, (vol) => {
  if (audioEl.value) {
    audioEl.value.volume = vol
  }
})

// Watch for volume changes
watch(volume, (vol) => {
  if (audioEl.value) {
    audioEl.value.volume = vol
  }
})

const togglePlay = () => {
  storeTogglePlay()
}

const toggleLyric = () => {
  showLyric.value = !showLyric.value
}

const seek = (e: Event) => {
  const target = e.target as HTMLInputElement
  const time = Number(target.value)
  if (audioEl.value) {
    audioEl.value.currentTime = time
  }
  setCurrentTime(time)
}

const handleProgressClick = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  const rect = target.querySelector('.progress-track')?.getBoundingClientRect()
  if (!rect || !audioEl.value) return
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  const newTime = percent * duration.value
  audioEl.value.currentTime = newTime
  setCurrentTime(newTime)
}

const handleProgressHover = (e: MouseEvent) => {
  if (!duration.value) return
  const target = e.currentTarget as HTMLElement
  const rect = target.querySelector('.progress-track')?.getBoundingClientRect()
  if (!rect) return
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  hoverPercent.value = percent * 100
  hoverTime.value = percent * duration.value
}

const showHoverTime = () => {
  isHovering.value = true
}

const hideHoverTime = () => {
  isHovering.value = false
}

const hideVolumeHover = () => {
  isVolumeHovering.value = false
}

const showVolumeBar = () => {
  isVolumeBarVisible.value = true
}

const hideVolumeBar = () => {
  isVolumeBarVisible.value = false
}

const toggleMute = () => {
  setVolume(volume.value > 0 ? 0 : 0.8)
}

const handleVolumeChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  setVolume(Number(target.value))
}

const handleVolumeClick = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  const rect = target.querySelector('.volume-track')?.getBoundingClientRect()
  if (!rect) return
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  setVolume(percent)
}

const handleVolumeHover = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  const rect = target.querySelector('.volume-track')?.getBoundingClientRect()
  if (!rect) return
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  volumeHoverPercent.value = percent * 100
  isVolumeHovering.value = true
}

const onTimeUpdate = () => {
  if (audioEl.value) {
    setCurrentTime(audioEl.value.currentTime)
  }
}

const onDurationChange = () => {
  if (audioEl.value && audioEl.value.duration) {
    player.setDuration(audioEl.value.duration)
  }
}

const onLoadedMetadata = () => {
  if (audioEl.value && audioEl.value.duration) {
    player.setDuration(audioEl.value.duration)
    console.log('Metadata loaded, duration:', audioEl.value.duration)
  }
}

const onEnded = () => {
  playNext()
}

const onError = (e: Event) => {
  const audio = e.target as HTMLAudioElement
  console.error('Audio error:', audio.error)
}

const onCanPlay = () => {
  console.log('Can play, starting playback')
  if (audioEl.value) {
    audioEl.value.play().catch(console.error)
  }
}

const formatTime = (seconds: number) => {
  if (!seconds || isNaN(seconds)) return '0:00'
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
  background: linear-gradient(180deg, var(--bg-card) 0%, var(--bg-dark) 100%);
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 20px;
  z-index: 100;
  backdrop-filter: blur(10px);
}

.player-left {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
  flex-shrink: 0;
}

.cover-wrapper {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: var(--transition);
  cursor: pointer;
}

.cover-wrapper:hover .cover-overlay {
  opacity: 1;
}

.song-info {
  min-width: 0;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 0 120px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.control-btn {
  color: var(--text-secondary);
  padding: 8px;
  border-radius: 50%;
  transition: var(--transition);
}

.control-btn:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.control-btn.active {
  color: var(--primary);
}

.play-btn {
  color: var(--primary);
  background: rgba(236, 65, 65, 0.15);
}

.play-btn:hover {
  background: rgba(236, 65, 65, 0.25);
  color: var(--primary);
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 800px;
}

.time {
  font-size: 11px;
  color: var(--text-muted);
  width: 40px;
  text-align: center;
}

.progress-bar {
  flex: 1;
  position: relative;
  height: 20px;
  display: flex;
  align-items: center;
}

.progress-track {
  position: absolute;
  width: 100%;
  height: 4px;
  background: var(--bg-hover);
  border-radius: 2px;
  overflow: visible;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: var(--primary);
  border-radius: 2px;
  transition: height 0.1s ease;
}

.progress-hover {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.progress-knob {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: var(--primary);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.2s ease;
  box-shadow: 0 0 4px rgba(0,0,0,0.3);
}

.progress-bar:hover .progress-track {
  height: 6px;
}

.progress-bar:hover .progress-fill {
  border-radius: 3px;
}

.progress-bar:hover .progress-knob {
  opacity: 1;
}

.hover-time {
  position: absolute;
  bottom: 24px;
  transform: translateX(-50%);
  background: var(--bg-dark);
  color: var(--text-primary);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  pointer-events: none;
}

.preview-badge {
  font-size: 10px;
  color: #fff;
  background: #ff6b6b;
  padding: 2px 6px;
  border-radius: 3px;
  margin-left: -4px;
}

.player-right {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 200px;
  justify-content: flex-end;
  flex-shrink: 0;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.volume-bar {
  position: relative;
  width: 0;
  height: 20px;
  display: flex;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s ease, width 0.2s ease;
  overflow: hidden;
}

.volume-bar.visible {
  width: 80px;
  opacity: 1;
}

.volume-control:hover .volume-bar {
  width: 80px;
  opacity: 1;
}

.volume-track {
  position: absolute;
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.5);
}

.volume-fill {
  height: 100%;
  background: #ec4141;
  border-radius: 2px;
}

.volume-hover {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.volume-knob {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: #ec4141;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.2s ease;
  box-shadow: 0 1px 4px rgba(0,0,0,0.4);
}

.volume-bar:hover .volume-track {
  height: 6px;
}

.volume-bar:hover .volume-fill {
  border-radius: 3px;
}

.volume-bar:hover .volume-knob {
  opacity: 1;
}

.volume-input {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  margin: 0;
}
</style>
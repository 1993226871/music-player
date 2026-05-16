<template>
  <div class="search-page">
    <!-- Search Header -->
    <div class="search-header">
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索歌曲、歌手、专辑..."
          @keyup.enter="search"
          ref="searchInput"
        />
        <button v-if="keyword" class="clear-btn" @click="clearSearch">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <span>搜索中...</span>
    </div>

    <!-- Empty State -->
    <div v-else-if="!keyword" class="empty-state">
      <svg viewBox="0 0 24 24" width="64" height="64" fill="currentColor" opacity="0.3">
        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
      </svg>
      <p>输入关键词搜索音乐</p>
    </div>

    <!-- No Results -->
    <div v-else-if="songs.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" width="64" height="64" fill="currentColor" opacity="0.3">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
      </svg>
      <p>未找到相关音乐</p>
    </div>

    <!-- Results -->
    <div v-else class="results">
      <div class="results-header">
        <span>找到 {{ songs.length }} 首歌曲</span>
      </div>
      <div class="song-list">
        <div
          v-for="(song, index) in songs"
          :key="song.id"
          class="song-item"
          :class="{ playing: currentSong?.id === song.id }"
          @click="playSong(song)"
        >
          <div class="song-index">{{ index + 1 }}</div>
          <img :src="song.coverUrl" class="song-cover" @error="handleImageError" />
          <div class="song-info">
            <div class="song-name">{{ song.name }}</div>
            <div class="song-artist">{{ song.artist }}</div>
          </div>
          <div class="song-actions">
            <button class="action-btn" @click.stop="addToFavorites(song)" title="收藏">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </button>
            <button class="action-btn play-btn" @click.stop="playSong(song)" title="播放">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </button>
          </div>
          <div class="song-duration" v-if="song.duration">{{ formatDuration(song.duration) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import api from '../utils/api'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const keyword = ref('')
const songs = ref<any[]>([])
const loading = ref(false)
const player = usePlayerStore()
const { currentSong } = storeToRefs(player)

const search = async () => {
  if (!keyword.value.trim()) return

  loading.value = true
  try {
    const res = await api.get(`/api/songs/search?keyword=${keyword.value}`)
    const data = JSON.parse(res.data.data)
    const songList = data.result.songs?.slice(0, 30) || []

    // Get song IDs and fetch cover images via backend API
    const songIds = songList.map((s: any) => s.id).join(',')
    let coverMap: Record<string, string> = {}

    if (songIds) {
      try {
        const coverRes = await api.get(`/api/songs/details?ids=${songIds}`)
        coverMap = coverRes.data.data || {}
      } catch (e) {
        console.error('Failed to fetch cover images:', e)
      }
    }

    songs.value = songList.map((s: any) => {
      let cover = coverMap[s.id] || s.album?.picUrl || ''
      if (!cover && s.album?.picId) {
        cover = `https://p3.music.126.net/${s.album.picId}/300.jpg`
      }
      if (!cover) {
        cover = '/default-cover.png'
      }
      return {
        id: s.id,
        name: s.name,
        artist: s.artists?.[0]?.name || s.album?.artist?.name || '未知',
        album: s.album?.name || '',
        coverUrl: cover,
        duration: s.duration || 0
      }
    })
  } catch (e) {
    console.error('Search error:', e)
    songs.value = []
  } finally {
    loading.value = false
  }
}

const clearSearch = () => {
  keyword.value = ''
  songs.value = []
}

const playSong = async (song: any) => {
  // Just set the song, Player.vue will handle URL fetching and playback
  player.play(song)
}

const addToFavorites = async (song: any) => {
  try {
    await api.post('/api/favorites', { songId: song.id })
  } catch (e) {
    console.error('Failed to add favorite:', e)
  }
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = '/default-cover.png'
}

const formatDuration = (ms: number) => {
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.search-page {
  min-height: 100%;
}

.search-header {
  padding: 24px;
  background: var(--bg-dark);
  position: sticky;
  top: 0;
  z-index: 10;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  transition: var(--transition);
}

.search-box:focus-within {
  box-shadow: 0 0 0 2px var(--primary);
}

.search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
}

.search-box input::placeholder {
  color: var(--text-muted);
}

.clear-btn {
  color: var(--text-muted);
  padding: 4px;
  border-radius: 50%;
  transition: var(--transition);
}

.clear-btn:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  gap: 16px;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bg-card);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  gap: 16px;
  color: var(--text-muted);
}

.empty-state p {
  font-size: 15px;
}

.results {
  padding: 0 24px;
}

.results-header {
  padding: 16px 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.song-list {
  display: flex;
  flex-direction: column;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
}

.song-item:hover {
  background: var(--bg-hover);
}

.song-item.playing {
  background: var(--bg-card);
}

.song-item.playing .song-name {
  color: var(--primary);
}

.song-index {
  width: 24px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
}

.song-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.song-info {
  flex: 1;
  min-width: 0;
}

.song-name {
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: var(--transition);
}

.song-item:hover .song-actions {
  opacity: 1;
}

.action-btn {
  color: var(--text-muted);
  padding: 6px;
  border-radius: 50%;
  transition: var(--transition);
}

.action-btn:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.action-btn.play-btn:hover {
  color: var(--primary);
}

.song-duration {
  font-size: 12px;
  color: var(--text-muted);
  width: 45px;
  text-align: right;
}
</style>
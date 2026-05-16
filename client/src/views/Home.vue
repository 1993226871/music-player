<template>
  <div class="home-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>发现音乐</h1>
        <p>探索你喜欢的音乐世界</p>
      </div>
      <div class="hero-decoration"></div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <router-link to="/search" class="action-card">
        <div class="action-icon search-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
        </div>
        <span>搜索音乐</span>
      </router-link>
      <router-link to="/favorites" class="action-card">
        <div class="action-icon heart-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </div>
        <span>我的收藏</span>
      </router-link>
      <router-link to="/playlist/1" class="action-card">
        <div class="action-icon playlist-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z"/>
          </svg>
        </div>
        <span>创建歌单</span>
      </router-link>
    </div>

    <!-- Recommended Songs -->
    <div class="section">
      <div class="section-header">
        <h2>推荐歌曲</h2>
        <router-link to="/search" class="see-more">查看更多</router-link>
      </div>
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
      </div>
      <div v-else class="song-grid">
        <div
          v-for="song in songs"
          :key="song.id"
          class="song-card"
          @click="playSong(song)"
        >
          <div class="song-cover-wrapper">
            <img :src="song.coverUrl" class="song-cover" @error="handleImageError" />
            <div class="play-overlay">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </div>
          </div>
          <div class="song-title">{{ song.name }}</div>
          <div class="song-artist">{{ song.artist }}</div>
        </div>
      </div>
    </div>

    <!-- Current Playlist -->
    <div class="section" v-if="player.currentSong">
      <div class="section-header">
        <h2>正在播放</h2>
      </div>
      <div class="now-playing">
        <img :src="player.currentSong.coverUrl" class="now-playing-cover" />
        <div class="now-playing-info">
          <div class="now-playing-title">{{ player.currentSong.name }}</div>
          <div class="now-playing-artist">{{ player.currentSong.artist }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../utils/api'
import { usePlayerStore } from '../stores/player'

const player = usePlayerStore()
const songs = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get('/api/songs/search?keyword=周杰伦')
    const data = JSON.parse(res.data.data)
    songs.value = data.result.songs?.slice(0, 6).map((s: any) => {
      // Try different possible cover URL fields
      let cover = s.artists?.[0]?.img1v1Url || s.album?.picUrl || '/default-cover.png'
      if (!cover || cover === '/default-cover.png') {
        if (s.album?.picId) {
          cover = `https://p3.music.126.net/${s.album.picId}/300.jpg`
        }
      }
      return {
        id: s.id,
        name: s.name,
        artist: s.artists?.[0]?.name || '未知',
        coverUrl: cover
      }
    }) || []
  } catch (e) {
    console.error('Failed to load songs:', e)
  } finally {
    loading.value = false
  }
})

const playSong = async (song: any) => {
  player.play(song)
  try {
    const res = await api.get(`/api/songs/${song.id}/url`)
    console.log('Song URL:', res.data.data.url)
  } catch (e) {
    console.error('Failed to get song URL:', e)
  }
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = '/default-cover.png'
}
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

.hero-section {
  position: relative;
  padding: 60px 24px 40px;
  background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-darker) 100%);
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-section h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--primary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-section p {
  color: var(--text-secondary);
  font-size: 16px;
}

.hero-decoration {
  position: absolute;
  top: -100px;
  right: -100px;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, var(--primary) 0%, transparent 70%);
  opacity: 0.1;
  border-radius: 50%;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 24px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  transition: var(--transition);
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  color: var(--text-primary);
}

.search-icon {
  background: linear-gradient(135deg, var(--primary) 0%, #ff6b6b 100%);
}

.heart-icon {
  background: linear-gradient(135deg, #ff6b6b 0%, var(--primary) 100%);
}

.playlist-icon {
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
}

.action-card span {
  font-size: 13px;
  color: var(--text-secondary);
}

.section {
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
}

.see-more {
  font-size: 12px;
  color: var(--text-muted);
  transition: var(--transition);
}

.see-more:hover {
  color: var(--primary);
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
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

.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.song-card {
  cursor: pointer;
  transition: var(--transition);
}

.song-card:hover {
  transform: translateY(-4px);
}

.song-cover-wrapper {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 12px;
}

.song-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition);
}

.song-card:hover .song-cover {
  transform: scale(1.05);
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: var(--transition);
}

.song-card:hover .play-overlay {
  opacity: 1;
}

.play-overlay svg {
  color: white;
}

.song-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.now-playing {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.now-playing-cover {
  width: 80px;
  height: 80px;
  border-radius: var(--radius);
  object-fit: cover;
}

.now-playing-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.now-playing-artist {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
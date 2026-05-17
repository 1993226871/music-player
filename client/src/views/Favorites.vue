<template>
  <div class="favorites-page">
    <h1>我的收藏</h1>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../utils/api'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const songs = ref<any[]>([])
const player = usePlayerStore()

onMounted(async () => {
  try {
    const res = await api.get('/api/favorites')
    const favorites = res.data.data || []

    if (favorites.length === 0) {
      songs.value = []
      return
    }

    // 获取歌曲 IDs 并获取详情
    const songIds = favorites.map((f: any) => f.songId).join(',')
    const detailsRes = await api.get(`/api/songs/details?ids=${songIds}`)

    const coverMap = detailsRes.data.data || {}
    songs.value = favorites.map((f: any) => ({
      id: f.songId,
      name: f.songName || '未知歌曲',
      artist: f.artist || '未知艺术家',
      coverUrl: coverMap[f.songId] || '/default-cover.png'
    }))
  } catch (e) {
    console.error('Failed to load favorites:', e)
    songs.value = []
  }
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.favorites-page { padding: 20px; }
</style>
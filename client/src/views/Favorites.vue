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
  const res = await api.get('/api/favorites')
  const favorites = res.data.data
  songs.value = favorites.map((f: any) => ({
    id: f.songId,
    name: '歌曲',
    artist: '未知',
    coverUrl: '/default-cover.png'
  }))
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.favorites-page { padding: 20px; }
</style>
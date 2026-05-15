<template>
  <div class="home">
    <h1>发现音乐</h1>
    <div class="recommend-section">
      <h2>推荐歌曲</h2>
      <SongList :songs="songs" @play="playSong" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const player = usePlayerStore()
const songs = ref<any[]>([])

const playSong = (song: any) => {
  player.play(song)
  fetchSongUrl(song.id)
}

const fetchSongUrl = async (songId: string) => {
  const res = await axios.get(`http://localhost:8080/api/songs/${songId}/url`)
  console.log('Song URL:', res.data.data.url)
}

onMounted(async () => {
  const res = await axios.get('http://localhost:8080/api/songs/search?keyword=周杰伦')
  const data = JSON.parse(res.data.data)
  songs.value = data.result.songs?.slice(0, 10).map((s: any) => ({
    id: s.id,
    name: s.name,
    artist: s.artists?.[0]?.name || '未知',
    coverUrl: s.album?.picUrl || '/default-cover.png'
  })) || []
})
</script>

<style scoped>
.home { padding: 20px; }
</style>
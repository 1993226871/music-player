<template>
  <div class="search-page">
    <input v-model="keyword" placeholder="搜索歌曲/歌手" @keyup.enter="search" />
    <button @click="search">搜索</button>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const keyword = ref('')
const songs = ref<any[]>([])
const player = usePlayerStore()

const search = async () => {
  const res = await axios.get(`http://localhost:8080/api/songs/search?keyword=${keyword.value}`)
  const data = JSON.parse(res.data.data)
  songs.value = data.result.songs?.slice(0, 20).map((s: any) => ({
    id: s.id,
    name: s.name,
    artist: s.artists?.[0]?.name || '未知',
    coverUrl: s.album?.picUrl || '/default-cover.png'
  })) || []
}

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.search-page { padding: 20px; }
input { padding: 10px; width: 300px; }
button { padding: 10px 20px; margin-left: 10px; }
</style>
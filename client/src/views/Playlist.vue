<template>
  <div class="playlist-page">
    <h1>{{ playlistName }}</h1>
    <button @click="showAddSong = true">添加歌曲</button>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../utils/api'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const route = useRoute()
const playlistName = ref('')
const songs = ref<any[]>([])
const player = usePlayerStore()
const showAddSong = ref(false)

onMounted(async () => {
  const id = route.params.id
  const plRes = await api.get(`/api/playlists/${id}`)
  playlistName.value = plRes.data.data.name

  const songsRes = await api.get(`/api/playlists/${id}/songs`)
  songs.value = songsRes.data.data.map((s: any) => ({
    id: s.songId,
    name: '歌曲',
    artist: '未知',
    coverUrl: '/default-cover.png'
  }))
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.playlist-page { padding: 20px; }
</style>
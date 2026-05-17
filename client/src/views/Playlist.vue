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
  try {
    const id = route.params.id
    const plRes = await api.get(`/api/playlists/${id}`)
    playlistName.value = plRes.data.data.name

    const songsRes = await api.get(`/api/playlists/${id}/songs`)
    const playlistSongs = songsRes.data.data || []

    if (playlistSongs.length === 0) {
      songs.value = []
      return
    }

    // 获取歌曲详情（封面）
    const songIds = playlistSongs.map((s: any) => s.songId).join(',')
    const detailsRes = await api.get(`/api/songs/details?ids=${songIds}`)

    const coverMap = detailsRes.data.data || {}
    songs.value = playlistSongs.map((s: any) => ({
      id: s.songId,
      name: s.songName || '未知歌曲',
      artist: s.artist || '未知艺术家',
      coverUrl: coverMap[s.songId] || '/default-cover.png'
    }))
  } catch (e) {
    console.error('Failed to load playlist:', e)
    songs.value = []
  }
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.playlist-page { padding: 20px; }
</style>
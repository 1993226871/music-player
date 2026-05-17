<template>
  <div class="song-list">
    <div v-for="song in songs" :key="song.id" class="song-item" @click="handleClick(song)">
      <img :src="song.coverUrl" class="song-cover" @error="handleImageError" />
      <div class="song-detail">
        <div class="song-name">{{ song.name }}</div>
        <div class="artist">{{ song.artist }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Song {
  id: string
  name: string
  artist: string
  coverUrl: string
}

defineProps<{ songs: Song[] }>()
const emit = defineEmits<{ (e: 'play', song: Song): void }>()

const handleClick = (song: Song) => emit('play', song)

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = '/default-cover.png'
}
</script>

<style scoped>
.song-list { display: flex; flex-direction: column; gap: 10px; }
.song-item { display: flex; align-items: center; padding: 10px; cursor: pointer; }
.song-item:hover { background: #f5f5f5; }
.song-cover { width: 50px; height: 50px; }
.song-detail { margin-left: 10px; }
</style>
<template>
  <div class="playlist-card" @click="handleClick">
    <img :src="coverUrl || '/default-cover.png'" class="cover" @error="handleImageError" />
    <div class="name">{{ name }}</div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  id: number
  name: string
  coverUrl?: string
}>()

const emit = defineEmits<{ (e: 'click', id: number): void }>()
const handleClick = () => emit('click', props.id)
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = '/default-cover.png'
}
</script>

<style scoped>
.playlist-card {
  cursor: pointer;
  width: 150px;
}
.playlist-card:hover { opacity: 0.8; }
.cover { width: 150px; height: 150px; border-radius: 8px; }
.name { margin-top: 8px; font-size: 14px; text-align: center; }
</style>
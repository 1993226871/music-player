import { defineStore } from 'pinia'
import { ref } from 'vue'

interface Song {
  id: string
  name: string
  artist: string
  album: string
  coverUrl: string
}

export const usePlayerStore = defineStore('player', () => {
  const currentSong = ref<Song | null>(null)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.8)
  const playlist = ref<Song[]>([])
  const currentIndex = ref(-1)
  const lyricLines = ref<{ time: number; text: string }[]>([])
  const showLyric = ref(false)
  const isPreview = ref(false)

  const play = (song: Song) => {
    currentSong.value = song
    isPlaying.value = true
  }

  const pause = () => {
    isPlaying.value = false
  }

  const togglePlay = () => {
    isPlaying.value = !isPlaying.value
  }

  const setCurrentTime = (time: number) => {
    currentTime.value = time
  }

  const setDuration = (dur: number) => {
    duration.value = dur
  }

  const setVolume = (vol: number) => {
    volume.value = vol
  }

  const setIsPlaying = (playing: boolean) => {
    isPlaying.value = playing
  }

  const setIsPreview = (preview: boolean) => {
    isPreview.value = preview
  }

  const playNext = () => {
    if (currentIndex.value < playlist.value.length - 1) {
      currentIndex.value++
      play(playlist.value[currentIndex.value])
    }
  }

  const playPrev = () => {
    if (currentIndex.value > 0) {
      currentIndex.value--
      play(playlist.value[currentIndex.value])
    }
  }

  return {
    currentSong, isPlaying, currentTime, duration, volume, isPreview,
    playlist, currentIndex, lyricLines, showLyric,
    play, pause, togglePlay, setCurrentTime, setDuration, setVolume, setIsPlaying, setIsPreview, playNext, playPrev
  }
})
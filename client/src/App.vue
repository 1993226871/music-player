<template>
  <div id="app">
    <router-view />
    <Player />
    <LyricDisplay />
  </div>
</template>

<script setup lang="ts">
import Player from './components/Player.vue'
import LyricDisplay from './components/LyricDisplay.vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
auth.initAuth()
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
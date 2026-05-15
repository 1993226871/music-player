import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    { path: '/', component: () => import('../views/Home.vue') },
    { path: '/search', component: () => import('../views/Search.vue') },
    { path: '/playlist/:id', component: () => import('../views/Playlist.vue') },
    { path: '/favorites', component: () => import('../views/Favorites.vue') }
  ]
})

export default router
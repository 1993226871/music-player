import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const publicPaths = ['/login', '/register', '/settings']

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    { path: '/', component: () => import('../views/Home.vue') },
    { path: '/search', component: () => import('../views/Search.vue') },
    { path: '/playlist/:id', component: () => import('../views/Playlist.vue') },
    { path: '/favorites', component: () => import('../views/Favorites.vue') },
    { path: '/admin', component: () => import('../views/Admin.vue') },
    { path: '/settings', component: () => import('../views/Settings.vue') }
  ]
})

// Skip auth check for public paths
const isPublicPath = (path: string) => publicPaths.includes(path)

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()

  // Wait for auth initialization if not ready
  if (!auth.isReady) {
    await auth.initAuth()
  }

  if (!isPublicPath(to.path) && !auth.token) {
    next('/login')
  } else if (isPublicPath(to.path) && auth.token) {
    next('/')
  } else {
    next()
  }
})

export default router
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/notes'
      },
      {
        path: 'notes',
        name: 'NoteList',
        component: () => import('@/views/NoteList.vue')
      },
      {
        path: 'notes/new',
        name: 'NoteNew',
        component: () => import('@/views/NoteEditor.vue')
      },
      {
        path: 'notes/:id',
        name: 'NoteEditor',
        component: () => import('@/views/NoteEditor.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = localStorage.getItem('token')

  if (token) {
    authStore.token = token
    authStore.isLoggedIn = true
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isLoggedIn) {
    next('/notes')
  } else {
    next()
  }
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import NoteList from '../views/NoteList.vue'
import NoteDetail from '../views/NoteDetail.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { 
    path: '/notes', 
    component: NoteList,
    meta: { requiresAuth: true }
  },
  {
    path: '/notes/:id',
    name: 'NoteDetail',
    component: NoteDetail,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userId = localStorage.getItem('userId')
  
  if (to.meta.requiresAuth && !userId) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && userId) {
    next('/notes')
  } else {
    next()
  }
})

export default router
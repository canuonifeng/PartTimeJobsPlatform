import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/registrations',
    name: 'Registrations',
    component: () => import('../views/registrations/RegistrationList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/job-reports',
    name: 'JobReports',
    component: () => import('../views/reports/JobReportList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/configs',
    name: 'Configs',
    component: () => import('../views/configs/SystemConfigList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/categories/JobCategoryList.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

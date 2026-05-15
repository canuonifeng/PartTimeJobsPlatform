import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true, title: '首页' }
  },
  {
    path: '/jobs',
    name: 'JobList',
    component: () => import('../views/jobs/JobList.vue'),
    meta: { requiresAuth: true, title: '职位管理' }
  },
  {
    path: '/jobs/create',
    name: 'JobCreate',
    component: () => import('../views/jobs/JobForm.vue'),
    meta: { requiresAuth: true, title: '新建职位' }
  },
  {
    path: '/jobs/:id/edit',
    name: 'JobEdit',
    component: () => import('../views/jobs/JobForm.vue'),
    meta: { requiresAuth: true, title: '编辑职位' },
    props: true
  },
  {
    path: '/applications',
    name: 'ApplicationList',
    component: () => import('../views/applications/ApplicationList.vue'),
    meta: { requiresAuth: true, title: '应聘管理' }
  },
  {
    path: '/schedules/templates',
    name: 'ScheduleTemplateList',
    component: () => import('../views/schedules/ScheduleTemplateList.vue'),
    meta: { requiresAuth: true, title: '排班模板' }
  },
  {
    path: '/schedules/shifts',
    name: 'ScheduleShiftList',
    component: () => import('../views/schedules/ScheduleShiftList.vue'),
    meta: { requiresAuth: true, title: '班次管理' }
  },
  {
    path: '/payroll',
    name: 'PayrollBatchList',
    component: () => import('../views/payroll/PayrollBatchList.vue'),
    meta: { requiresAuth: true, title: '薪资批次' }
  },
  {
    path: '/payroll/:id',
    name: 'PayrollBatchDetail',
    component: () => import('../views/payroll/PayrollBatchDetail.vue'),
    meta: { requiresAuth: true, title: '批次详情' },
    props: true
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
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router

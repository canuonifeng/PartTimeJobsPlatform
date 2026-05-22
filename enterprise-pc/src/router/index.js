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
    path: '/schedules/shifts',
    name: 'ScheduleShiftList',
    component: () => import('../views/schedules/ScheduleShiftList.vue'),
    meta: { requiresAuth: true, title: '排班管理' }
  },
  {
    path: '/attendance/hours',
    name: 'AttendanceHoursList',
    component: () => import('../views/attendance/AttendanceHoursList.vue'),
    meta: { requiresAuth: true, title: '考勤管理' }
  },
  {
    path: '/settlement/bills',
    name: 'SettlementBillList',
    component: () => import('../views/settlement/SettlementBillList.vue'),
    meta: { requiresAuth: true, title: '结算账单' }
  },
  {
    path: '/workers',
    name: 'WorkerList',
    component: () => import('../views/workers/WorkerList.vue'),
    meta: { requiresAuth: true, title: '兼职管理' }
  },
  {
    path: '/accounts',
    name: 'AccountList',
    component: () => import('../views/accounts/AccountList.vue'),
    meta: { requiresAuth: true, title: '账号管理' }
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

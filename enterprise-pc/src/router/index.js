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
    meta: { requiresAuth: true, title: '零工招聘' }
  },
  {
    path: '/jobs/annotation',
    name: 'AnnotationJobList',
    component: () => import('../views/jobs/AnnotationJobList.vue'),
    meta: { requiresAuth: true, title: '标注任务' }
  },
  {
    path: '/jobs/annotation/:id/batches',
    name: 'AnnotationBatchList',
    component: () => import('../views/jobs/AnnotationBatchList.vue'),
    meta: { requiresAuth: true, title: '批次管理' },
    props: true
  },
  {
    path: '/jobs/create',
    name: 'JobCreate',
    component: () => import('../views/jobs/JobForm.vue'),
    meta: { requiresAuth: true, title: '新建招聘计划' }
  },
  {
    path: '/jobs/:id/edit',
    name: 'JobEdit',
    component: () => import('../views/jobs/JobForm.vue'),
    meta: { requiresAuth: true, title: '编辑招聘计划' },
    props: true
  },
  {
    path: '/applications',
    name: 'ApplicationList',
    component: () => import('../views/applications/ApplicationList.vue'),
    meta: { requiresAuth: true, title: '报名审核' }
  },
  {
    path: '/schedules/shifts',
    name: 'ScheduleShiftList',
    component: () => import('../views/schedules/ScheduleShiftList.vue'),
    meta: { requiresAuth: true, title: '考勤确认' }
  },
  {
    path: '/schedules',
    name: 'ScheduleManagement',
    component: () => import('../views/schedules/ScheduleManagement.vue'),
    meta: { requiresAuth: true, title: '班次管理' }
  },
  {
    path: '/attendance/hours',
    name: 'AttendanceHoursList',
    component: () => import('../views/attendance/AttendanceHoursList.vue'),
    meta: { requiresAuth: true, title: '薪资结算' }
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
  },
  {
    path: '/accounts/password',
    name: 'PasswordChange',
    component: () => import('../views/accounts/PasswordChange.vue'),
    meta: { requiresAuth: true, title: '修改密码' }
  },
  {
    path: '/templates',
    name: 'TemplateList',
    component: () => import('../views/templates/TemplateList.vue'),
    meta: { requiresAuth: true, title: '招聘模板' }
  },
  {
    path: '/jobs/task-orders',
    name: 'TaskOrderList',
    component: () => import('../views/jobs/TaskOrderList.vue'),
    meta: { requiresAuth: true, title: '任务单管理' }
  },
  {
    path: '/jobs/worker-mapping',
    name: 'ExternalWorkerMapping',
    component: () => import('../views/jobs/ExternalWorkerMapping.vue'),
    meta: { requiresAuth: true, title: '人员映射' }
  },
  {
    path: '/locations',
    name: 'LocationList',
    component: () => import('../views/locations/LocationList.vue'),
    meta: { requiresAuth: true, title: '工作地点' }
  },
  {
    path: '/settings',
    name: 'CompanySettings',
    component: () => import('../views/settings/CompanySettings.vue'),
    meta: { requiresAuth: true, title: '企业设置' }
  },
  {
    path: '/balance',
    name: 'BalancePage',
    component: () => import('../views/balance/BalancePage.vue'),
    meta: { requiresAuth: true, title: '账户余额' }
  },
  {
    path: '/auth/real-name',
    name: 'RealNameAuth',
    component: () => import('../views/auth/RealNameAuth.vue'),
    meta: { requiresAuth: true, title: '实名认证' }
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

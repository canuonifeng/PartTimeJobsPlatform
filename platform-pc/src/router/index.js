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
       path: '/jobs',
       name: 'Jobs',
       component: () => import('../views/jobs/JobList.vue'),
       meta: { requiresAuth: true }
     },
     {
       path: '/applications',
       name: 'Applications',
       component: () => import('../views/applications/ApplicationList.vue'),
       meta: { requiresAuth: true }
     },
     {
       path: '/attendance',
       name: 'Attendance',
       component: () => import('../views/attendance/AttendanceList.vue'),
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
    },
    {
      path: '/job-tags',
      name: 'JobTags',
      component: () => import('../views/tags/JobTagList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/enterprises',
      name: 'Enterprises',
      component: () => import('../views/enterprises/EnterpriseList.vue'),
      meta: { requiresAuth: true }
    },
     {
       path: '/admin/workers',
       name: 'WorkerList',
       component: () => import('../views/workers/WorkerList.vue'),
       meta: { requiresAuth: true }
     },
     {
       path: '/withdrawals',
       name: 'WithdrawalRecords',
       component: () => import('../views/finance/WithdrawalList.vue'),
       meta: { requiresAuth: true }
     },
     {
       path: '/complaints',
       name: 'Complaints',
       component: () => import('../views/risk/ComplaintList.vue'),
       meta: { requiresAuth: true }
     },
    {
      path: '/auth/workers',
      name: 'WorkerRealNameList',
      component: () => import('../views/auth/WorkerRealNameList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/auth/enterprises',
      name: 'EnterpriseRealNameList',
      component: () => import('../views/auth/EnterpriseRealNameList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/referral/config',
      name: 'ReferralConfig',
      component: () => import('../views/referral/ReferralConfig.vue'),
      meta: { requiresAuth: true, title: '奖励规则配置' }
    },
    {
      path: '/referral/audit',
      name: 'ReferralAudit',
      component: () => import('../views/referral/ReferralAudit.vue'),
      meta: { requiresAuth: true, title: '奖励审核' }
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

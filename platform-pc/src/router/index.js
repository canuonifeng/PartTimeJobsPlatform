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
        path: '/schedules',
        name: 'Schedules',
        component: () => import('../views/schedules/ScheduleList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/attendance',
        name: 'Attendance',
        component: () => import('../views/attendance/AttendanceList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/settlements',
        name: 'Settlements',
        component: () => import('../views/settlements/SettlementList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/top-up',
        name: 'TopUp',
        component: () => import('../views/finance/TopUpList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/finance-report',
        name: 'FinanceReport',
        component: () => import('../views/finance/FinanceReport.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/transactions',
        name: 'Transactions',
        component: () => import('../views/finance/TransactionList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/risk-center',
        name: 'RiskCenter',
        component: () => import('../views/risk/RiskCenter.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/reviews',
        name: 'Reviews',
        component: () => import('../views/risk/ReviewList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/report-center',
        name: 'ReportCenter',
        component: () => import('../views/reports/ReportCenter.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/activities',
        name: 'Activities',
        component: () => import('../views/operation/ActivityManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/content-operation',
        name: 'ContentOperation',
        component: () => import('../views/config/ContentOperation.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/operators',
        name: 'Operators',
        component: () => import('../views/config/OperatorManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/operation-logs',
        name: 'OperationLogs',
        component: () => import('../views/config/OperationLog.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/customer-service',
        name: 'CustomerService',
        component: () => import('../views/cs/CustomerService.vue'),
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
    },
    {
      path: '/training/certifications',
      name: 'TrainingCertifications',
      component: () => import('../views/training/TrainingCertificationList.vue'),
      meta: { requiresAuth: true, title: '技能认证管理' }
    },
    {
      path: '/training/courses',
      name: 'TrainingCourses',
      component: () => import('../views/training/TrainingCourseList.vue'),
      meta: { requiresAuth: true, title: '培训课程管理' }
    },
    {
      path: '/training/question-banks',
      name: 'TrainingQuestionBanks',
      component: () => import('../views/training/QuestionBankList.vue'),
      meta: { requiresAuth: true, title: '题库管理' }
    },
    {
      path: '/jobs/detail',
      name: 'JobDetail',
      component: () => import('../views/jobs/JobDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/risk/complaints/detail',
      name: 'ComplaintDetail',
      component: () => import('../views/risk/ComplaintDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cs/faqs',
      name: 'FaqManage',
      component: () => import('../views/cs/FaqManage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cs/tickets',
      name: 'TicketHandle',
      component: () => import('../views/cs/TicketHandle.vue'),
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

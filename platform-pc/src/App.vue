<template>
  <router-view v-if="!isLoggedIn" />
  <el-container v-else class="layout-container">
    <el-aside width="220px">
      <el-menu :default-active="currentRoute" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <div class="sidebar-logo">管理后台</div>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="operations">
          <template #title><el-icon><Folder /></el-icon><span>职位运营</span></template>
          <el-menu-item index="/jobs">职位管理</el-menu-item>
          <el-menu-item index="/applications">报名审核</el-menu-item>
          <el-menu-item index="/attendance">考勤管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/job-reports">
          <el-icon><Warning /></el-icon><span>职位举报</span>
        </el-menu-item>
        <el-menu-item index="/configs">
          <el-icon><Setting /></el-icon><span>系统配置</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Folder /></el-icon><span>职位分类</span>
        </el-menu-item>
        <el-menu-item index="/job-tags">
          <el-icon><CollectionTag /></el-icon><span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/enterprises">
          <el-icon><OfficeBuilding /></el-icon><span>企业管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/workers">
          <el-icon><User /></el-icon><span>兼职管理</span>
        </el-menu-item>
        <el-sub-menu index="finance">
          <template #title><el-icon><Money /></el-icon><span>交易财务</span></template>
          <el-menu-item index="/withdrawals">提现审核</el-menu-item>
          <el-menu-item index="/top-up">企业充值</el-menu-item>
          <el-menu-item index="/finance-report">财务对账</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="risk">
          <template #title><el-icon><Warning /></el-icon><span>风控治理</span></template>
          <el-menu-item index="/complaints">投诉工单</el-menu-item>
          <el-menu-item index="/risk-center">风控中心</el-menu-item>
          <el-menu-item index="/reviews">评价管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="data">
          <template #title><el-icon><DataAnalysis /></el-icon><span>数据运营</span></template>
          <el-menu-item index="/report-center">报表中心</el-menu-item>
          <el-menu-item index="/activities">活动运营</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="config-new">
          <template #title><el-icon><Setting /></el-icon><span>平台配置</span></template>
          <el-menu-item index="/configs">系统配置</el-menu-item>
          <el-menu-item index="/content-operation">内容运营</el-menu-item>
          <el-menu-item index="/operators">账号权限</el-menu-item>
          <el-menu-item index="/operation-logs">操作日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="cs">
          <template #title><el-icon><Avatar /></el-icon><span>客服中心</span></template>
          <el-menu-item index="/customer-service">在线客服</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/auth/workers">
          <el-icon><Avatar /></el-icon><span>实名审核-兼职</span>
        </el-menu-item>
        <el-menu-item index="/auth/enterprises">
          <el-icon><Postcard /></el-icon><span>实名审核-企业</span>
        </el-menu-item>
        <el-menu-item index="/referral/config">
          <el-icon><Setting /></el-icon><span>邀请奖励配置</span>
        </el-menu-item>
        <el-menu-item index="/referral/audit">
          <el-icon><Money /></el-icon><span>邀请奖励审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span class="header-title">管理后台</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { DataAnalysis, Warning, Setting, Folder, CollectionTag, OfficeBuilding, User, Money, Avatar, Postcard } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isAuthenticated())
const currentRoute = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.layout-container {
  height: 100vh;
}
.sidebar-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: #2b3643;
}
.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.layout-main {
  background: #f0f2f5;
  padding: 20px;
}
.el-aside {
  overflow: auto;
  height: 100%;
  background: #304156;
}
</style>

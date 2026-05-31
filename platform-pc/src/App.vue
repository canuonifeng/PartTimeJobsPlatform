<template>
  <router-view v-if="!isLoggedIn" />
  <el-container v-else class="layout-container">
    <el-aside width="220px">
      <el-menu :default-active="currentRoute" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <div class="sidebar-logo">管理后台</div>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/job-reports">
          <el-icon><Warning /></el-icon><span>职位举报</span>
        </el-menu-item>
        <el-menu-item index="/configs">
          <el-icon><Setting /></el-icon><span>系统配置</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Folder /></el-icon><span>职位分类</span>
        </el-menu-item>
        <el-menu-item index="/enterprises">
          <el-icon><OfficeBuilding /></el-icon><span>企业管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/workers">
          <el-icon><User /></el-icon><span>兼职管理</span>
        </el-menu-item>
        <el-menu-item index="/withdrawals">
          <el-icon><Money /></el-icon><span>提现记录</span>
        </el-menu-item>
        <el-menu-item index="/auth/workers">
          <el-icon><Avatar /></el-icon><span>实名审核-兼职</span>
        </el-menu-item>
        <el-menu-item index="/auth/enterprises">
          <el-icon><Postcard /></el-icon><span>实名审核-企业</span>
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
import { DataAnalysis, Warning, Setting, Folder, OfficeBuilding, User, Money, Avatar, Postcard } from '@element-plus/icons-vue'

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

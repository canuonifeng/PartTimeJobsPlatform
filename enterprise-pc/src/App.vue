<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoginPage = computed(() => route.path === '/login')

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function handleMenuSelect(index) {
  router.push(index)
}
</script>

<template>
  <div v-if="isLoginPage" class="login-wrapper">
    <router-view />
  </div>
  <el-container v-else class="app-container">
    <el-aside width="220px" class="app-aside">
      <div class="logo">企业管理后台</div>
      <el-menu
        :default-active="route.path"
        @select="handleMenuSelect"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/jobs">
          <el-icon><Briefcase /></el-icon>
          <span>职位管理</span>
        </el-menu-item>
        <el-menu-item index="/applications">
          <el-icon><Document /></el-icon>
          <span>应聘管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules/shifts">
          <el-icon><Calendar /></el-icon>
          <span>排班考勤</span>
        </el-menu-item>
        <el-menu-item index="/attendance/hours">
          <el-icon><Money /></el-icon>
          <span>薪资管理</span>
        </el-menu-item>
        <el-menu-item index="/workers">
          <el-icon><User /></el-icon>
          <span>兼职管理</span>
        </el-menu-item>
        <el-menu-item index="/accounts">
          <el-icon><Setting /></el-icon>
          <span>账号管理</span>
        </el-menu-item>
        <el-menu-item index="/locations">
          <el-icon><Location /></el-icon>
          <span>工作地点</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><CopyDocument /></el-icon>
          <span>职位模版</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Picture /></el-icon>
          <span>企业设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <span class="header-title">{{ route.meta.title || '企业管理后台' }}</span>
        <div class="header-right">
          <span class="user-info">{{ authStore.user?.username || '管理员' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>

<style scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.app-container {
  height: 100vh;
}
.app-aside {
  background-color: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  color: #606266;
}
.app-main {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>

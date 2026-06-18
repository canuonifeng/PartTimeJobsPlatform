<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoginPage = computed(() => route.path === '/login')
const displayName = computed(() => authStore.user?.displayName || authStore.user?.username || '管理员')
const companyName = computed(() => authStore.user?.companyName || '企业管理后台')
const roleName = computed(() => authStore.user?.role || 'ADMIN')

watch(
  () => route.path,
  () => {
    if (!isLoginPage.value && authStore.token && !authStore.user) {
      authStore.loadCurrentUser().catch(() => authStore.logout())
    }
  },
  { immediate: true }
)

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
        <el-menu-item index="/schedules">
          <el-icon><Calendar /></el-icon>
          <span>班次管理</span>
        </el-menu-item>
        <el-menu-item index="/jobs">
          <el-icon><Briefcase /></el-icon>
          <span>招聘计划</span>
        </el-menu-item>
        <el-menu-item index="/applications">
          <el-icon><Document /></el-icon>
          <span>报名审核</span>
        </el-menu-item>
        <el-menu-item index="/schedules/shifts">
          <el-icon><Calendar /></el-icon>
          <span>考勤确认</span>
        </el-menu-item>
        <el-menu-item index="/attendance/hours">
          <el-icon><Money /></el-icon>
          <span>薪资结算</span>
        </el-menu-item>
        <el-menu-item index="/workers">
          <el-icon><User /></el-icon>
          <span>兼职管理</span>
        </el-menu-item>
        <el-menu-item index="/accounts">
          <el-icon><Setting /></el-icon>
          <span>账号管理</span>
        </el-menu-item>
        <el-menu-item index="/accounts/password">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </el-menu-item>
        <el-menu-item index="/locations">
          <el-icon><Location /></el-icon>
          <span>工作地点</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><CopyDocument /></el-icon>
          <span>招聘模板</span>
        </el-menu-item>
        <el-menu-item index="/balance">
          <el-icon><Wallet /></el-icon>
          <span>账户余额</span>
        </el-menu-item>
        <el-menu-item index="/auth/real-name">
          <el-icon><Postcard /></el-icon>
          <span>实名认证</span>
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
          <div class="enterprise-info">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ companyName }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="user-info">
            <el-avatar :size="28">{{ displayName.slice(0, 1) }}</el-avatar>
            <div class="user-text">
              <span class="user-name">{{ displayName }}</span>
              <span class="user-role">{{ roleName }}</span>
            </div>
          </div>
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
.enterprise-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}
.user-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.user-name {
  color: #303133;
  font-size: 14px;
}
.user-role {
  color: #909399;
  font-size: 12px;
}
.app-main {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>

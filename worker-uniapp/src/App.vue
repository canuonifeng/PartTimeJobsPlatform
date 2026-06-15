<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './store'
import { refreshMessageTabBarBadge } from './utils/notificationBadge'

const authStore = useAuthStore()

async function routeBySession() {
  await authStore.loadSession()
  const target = authStore.token ? '/pages/index/index' : '/pages/jobs/jobList'
  uni.switchTab({ url: target })
}

onLaunch(() => {
  console.log('App Launch')
  routeBySession()
  refreshMessageTabBarBadge()
})
onShow(() => {
  console.log('App Show')
  refreshMessageTabBarBadge()
})
onHide(() => {
  console.log('App Hide')
})
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Arial, sans-serif;
  color: #333;
  font-size: 14px;
  box-sizing: border-box;
}

view {
  box-sizing: border-box;
}

::-webkit-scrollbar {
  display: none;
}
</style>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './store'
import { refreshMessageTabBarBadge } from './utils/notificationBadge'

const authStore = useAuthStore()

async function routeBySession() {
  await authStore.loadSession()
  uni.switchTab({ url: '/pages/jobs/jobList' })
}

function saveInviteCodeFromQuery(query) {
  if (!query) return
  const code = query.inviteCode || query.referralCode || query.code
  if (code && typeof code === 'string') {
    uni.setStorageSync('inviteCode', code)
  }
}

onLaunch((options) => {
  console.log('App Launch')
  if (options && options.query) {
    saveInviteCodeFromQuery(options.query)
  }
  routeBySession()
  refreshMessageTabBarBadge()
})
onShow((options) => {
  console.log('App Show')
  if (options && options.query) {
    saveInviteCodeFromQuery(options.query)
  }
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

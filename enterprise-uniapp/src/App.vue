<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'

onLaunch(() => {
  const authStore = useAuthStore()
  authStore.init()

  uni.addInterceptor('navigateTo', {
    invoke(args) {
      const token = uni.getStorageSync('token')
      if (!token && !args.url.includes('/pages/login/login')) {
        uni.reLaunch({ url: '/pages/login/login' })
        return false
      }
    }
  })

  uni.addInterceptor('redirectTo', {
    invoke(args) {
      const token = uni.getStorageSync('token')
      if (!token && !args.url.includes('/pages/login/login')) {
        uni.reLaunch({ url: '/pages/login/login' })
        return false
      }
    }
  })

  uni.addInterceptor('reLaunch', {
    invoke(args) {
      const token = uni.getStorageSync('token')
      if (!token && !args.url.includes('/pages/login/login')) {
        uni.reLaunch({ url: '/pages/login/login' })
        return false
      }
    }
  })
})
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Arial, sans-serif;
  box-sizing: border-box;
}
</style>

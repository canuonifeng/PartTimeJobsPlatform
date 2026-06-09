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
  background-color: #f6f8f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Arial, sans-serif;
  box-sizing: border-box;
  color: #1f2933;
}

:root {
  --color-primary: #07c160;
  --color-primary-dark: #08a95a;
  --color-primary-light: #eafaf1;
  --color-bg: #f6f8f7;
  --color-card: #ffffff;
  --color-text: #1f2933;
  --color-text-secondary: #64748b;
  --color-text-muted: #98a3b3;
  --color-border: #edf0f3;
  --color-success: #07c160;
  --color-warning: #ff9500;
  --color-danger: #ff3b30;
  --color-info: #3b82f6;
  --radius-card: 24rpx;
  --radius-btn: 44rpx;
  --radius-badge: 999rpx;
  --shadow-card: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  --shadow-float: 0 8rpx 28rpx rgba(26, 35, 48, 0.12);
}

.btn-primary {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 44rpx;
  font-size: 31rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
  border: none;
}
.btn-primary::after { border: none; }
.btn-primary[disabled] {
  background: #c8e6d0;
  color: #fff;
}
</style>

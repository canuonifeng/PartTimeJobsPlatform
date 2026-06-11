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

.e-page { min-height: 100vh; background: #f6f8f7; color: #1f2933; box-sizing: border-box; }
.e-header { background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 48rpx 32rpx 32rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; color: #fff; box-sizing: border-box; }
.e-header-row { display: flex; align-items: center; justify-content: space-between; min-width: 0; }
.e-header-title { font-size: 36rpx; font-weight: 800; line-height: 1.3; color: #fff; }
.e-header-desc { display: block; margin-top: 10rpx; font-size: 24rpx; line-height: 1.5; color: rgba(255,255,255,.82); }
.e-content { padding: 24rpx 28rpx 40rpx; box-sizing: border-box; }
.e-card { background: #fff; border-radius: 24rpx; padding: 28rpx; box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08); box-sizing: border-box; overflow: hidden; }
.e-card + .e-card { margin-top: 22rpx; }
.e-card-title-row { display: flex; align-items: center; justify-content: space-between; min-width: 0; }
.e-card-title { flex: 1; min-width: 0; font-size: 31rpx; font-weight: 800; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.e-card-subtitle { display: block; margin-top: 8rpx; font-size: 24rpx; color: #64748b; line-height: 1.5; }
.e-badge { flex-shrink: 0; padding: 7rpx 18rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 700; line-height: 1.2; }
.e-badge-green { background: #e7f8ef; color: #08a857; }
.e-badge-orange { background: #fff7df; color: #d28a00; }
.e-badge-red { background: #ffecec; color: #e5484d; }
.e-badge-blue { background: #edf5ff; color: #3b82f6; }
.e-badge-gray { background: #eef1f0; color: #7b8580; }
.e-info-grid { display: flex; flex-wrap: wrap; margin: 20rpx -7rpx 0; }
.e-info-pill { width: calc(50% - 14rpx); min-width: 0; margin: 0 7rpx 14rpx; padding: 14rpx 16rpx; background: #f8faf9; border-radius: 16rpx; box-sizing: border-box; }
.e-info-label { display: block; font-size: 22rpx; color: #98a3b3; line-height: 1.4; }
.e-info-value { display: block; margin-top: 6rpx; font-size: 26rpx; font-weight: 700; color: #1f2933; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.e-action-row { display: flex; flex-wrap: wrap; margin: 22rpx -7rpx 0; }
.e-action-pill { flex: 1 1 150rpx; min-width: 0; margin: 0 7rpx 14rpx; height: 64rpx; line-height: 64rpx; text-align: center; border-radius: 999rpx; font-size: 25rpx; font-weight: 700; box-sizing: border-box; }
.e-action-primary { background: #e7f8ef; color: #08a857; }
.e-action-blue { background: #edf5ff; color: #3b82f6; }
.e-action-orange { background: #fff7df; color: #d28a00; }
.e-action-red { background: #ffecec; color: #e5484d; }
.e-action-gray { background: #eef1f0; color: #7b8580; }
.e-empty { padding: 100rpx 36rpx; text-align: center; color: #98a3b3; }
.e-empty-title { display: block; font-size: 30rpx; font-weight: 700; color: #64748b; }
.e-empty-desc { display: block; margin-top: 10rpx; font-size: 25rpx; color: #98a3b3; }
.e-form-section { background: #fff; border-radius: 24rpx; padding: 28rpx; box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08); box-sizing: border-box; margin-bottom: 22rpx; }
.e-section-title { display: block; margin-bottom: 22rpx; font-size: 30rpx; font-weight: 800; color: #1f2933; }
.e-form-row { margin-bottom: 24rpx; }
.e-form-row:last-child { margin-bottom: 0; }
.e-form-label { display: block; margin-bottom: 12rpx; font-size: 26rpx; font-weight: 700; color: #64748b; }
.e-input, .e-textarea, .e-picker-value { width: 100%; min-height: 84rpx; padding: 0 22rpx; border: 2rpx solid #edf0f3; border-radius: 18rpx; background: #f8faf9; box-sizing: border-box; font-size: 28rpx; color: #1f2933; }
.e-textarea { min-height: 180rpx; padding-top: 20rpx; line-height: 1.6; }
.e-picker-value { display: flex; align-items: center; justify-content: space-between; }
.e-bottom-safe { padding-bottom: 64rpx; }
</style>

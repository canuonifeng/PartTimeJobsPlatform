<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'

onLaunch(() => {
  const authStore = useAuthStore()
  authStore.init()

  const token = uni.getStorageSync('token')
  if (!token) {
    setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 100)
  }

  uni.addInterceptor('navigateTo', {
    invoke(args) {
      const t = uni.getStorageSync('token')
      if (!t && !args.url.includes('/pages/login/login')) {
        uni.redirectTo({ url: '/pages/login/login' })
        return false
      }
    }
  })

  uni.addInterceptor('redirectTo', {
    invoke(args) {
      const t = uni.getStorageSync('token')
      if (!t && !args.url.includes('/pages/login/login')) {
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

.op-page {
  min-height: 100vh;
  background: #f6f8f7;
  color: #1f2933;
  box-sizing: border-box;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

uni-tabbar,
.uni-tabbar-bottom,
.uni-tabbar-bottom .uni-tabbar {
  position: fixed !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 9999 !important;
}

.uni-tabbar-bottom .uni-tabbar {
  padding-bottom: env(safe-area-inset-bottom) !important;
}

.op-hero {
  margin: 0 28rpx;
  padding: 34rpx 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #18c86b 0%, #0f9f54 52%, #047857 100%);
  color: #fff;
  box-shadow: 0 18rpx 40rpx rgba(4, 120, 87, 0.18);
  box-sizing: border-box;
}

.op-hero-kicker {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
}

.op-hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 42rpx;
  font-weight: 850;
  line-height: 1.22;
  color: #fff;
}

.op-hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.82);
}

.op-content {
  padding: 24rpx 28rpx 48rpx;
  box-sizing: border-box;
}

.op-section {
  margin-top: 28rpx;
}

.op-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.op-section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #1f2933;
}

.op-section-link {
  font-size: 24rpx;
  font-weight: 700;
  color: #16a34a;
}

.op-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 26rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  box-sizing: border-box;
}

.op-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: #12834a;
  background: #ecfdf5;
}

.op-pill-warn {
  color: #b45309;
  background: #fffbeb;
}

.op-pill-danger {
  color: #dc2626;
  background: #fee2e2;
}

.op-row {
  display: flex;
  align-items: center;
  min-width: 0;
}

.op-row-main {
  flex: 1;
  min-width: 0;
}

.op-row-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1f2933;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.op-row-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  line-height: 1.45;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

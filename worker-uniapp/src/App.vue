<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './store'

const authStore = useAuthStore()

onLaunch(() => {
  console.log('App Launch')
  authStore.loadSession()
})
onShow(() => {
  console.log('App Show')
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

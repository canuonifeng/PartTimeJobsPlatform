import { getMyNotifications } from '@/api/notifications'
import { hasUnreadMessages } from './notificationBadges.mjs'

const MESSAGE_TAB_INDEX = 2

export function syncMessageTabBarBadge(messages) {
  if (hasUnreadMessages(messages)) {
    uni.showTabBarRedDot({ index: MESSAGE_TAB_INDEX })
  } else {
    uni.hideTabBarRedDot({ index: MESSAGE_TAB_INDEX })
  }
}

export async function refreshMessageTabBarBadge() {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.hideTabBarRedDot({ index: MESSAGE_TAB_INDEX })
    return
  }

  try {
    const res = await getMyNotifications({ page: 1, pageSize: 20 }, { authRedirect: false })
    const records = Array.isArray(res) ? res : (Array.isArray(res?.records) ? res.records : [])
    syncMessageTabBarBadge(records)
  } catch (e) {
    uni.hideTabBarRedDot({ index: MESSAGE_TAB_INDEX })
  }
}

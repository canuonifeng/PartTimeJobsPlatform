import request from './request'

export function getMyNotifications(params = {}) {
  return request({
    url: '/notifications/my',
    method: 'GET',
    data: params
  })
}

export function markNotificationRead(id) {
  return request({
    url: '/notifications/read',
    method: 'POST',
    data: { id }
  })
}

import request from './request'

export function getMyNotifications(params = {}) {
  return request({
    url: '/api/notifications/my',
    method: 'GET',
    data: params
  })
}

export function markNotificationRead(id) {
  return request({
    url: '/api/notifications/read',
    method: 'POST',
    data: { id }
  })
}

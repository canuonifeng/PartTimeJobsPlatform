import request from './request'

export function getMyNotifications(params = {}, options = {}) {
  return request({
    url: '/notifications/my',
    method: 'GET',
    data: params,
    ...options
  })
}

export function markNotificationRead(id) {
  return request({
    url: '/notifications/read',
    method: 'POST',
    data: { id }
  })
}

import { request } from './request'

export function getNotifications(recipientId, recipientType = 'ENTERPRISE') {
  const query = [`recipientId=${recipientId}`, `recipientType=${encodeURIComponent(recipientType)}`]
  return request('GET', `/notifications?${query.join('&')}`)
}

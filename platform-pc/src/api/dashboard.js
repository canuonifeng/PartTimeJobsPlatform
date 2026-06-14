import request from './request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

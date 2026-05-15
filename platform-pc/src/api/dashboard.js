import request from './request'

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

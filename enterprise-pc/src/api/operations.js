import request from './request'

export function getOperationDashboard() {
  return request.get('/operations/dashboard')
}

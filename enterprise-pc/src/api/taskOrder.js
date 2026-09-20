import request from './request'

export function listTaskOrders(params) {
  return request.get('/task-orders', { params })
}

export function getTaskOrder(id) {
  return request.get('/task-orders', { params: { id } })
}

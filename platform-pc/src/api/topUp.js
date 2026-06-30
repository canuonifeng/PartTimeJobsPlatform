import request from './request'

export function listTopUp(params) {
  return request.post('/top-up/list', params)
}

export function approveTopUp(id) {
  return request.post('/top-up/approve', { id })
}

export function rejectTopUp(id, remark) {
  return request.post('/top-up/reject', { id, remark })
}

import request from './request'

export function listWithdrawals(params) {
  return request.post('/withdrawals/list', params)
}

export function approveWithdrawal(id) {
  return request.post('/withdrawals/approve', { id })
}

export function rejectWithdrawal(id) {
  return request.post('/withdrawals/reject', { id })
}

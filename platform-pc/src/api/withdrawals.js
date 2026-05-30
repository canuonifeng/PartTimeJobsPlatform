import request from './request'

export function listWithdrawals(params) {
  return request.get('/admin/withdrawals', { params })
}

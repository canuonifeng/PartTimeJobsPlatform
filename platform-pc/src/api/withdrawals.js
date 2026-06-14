import request from './request'

export function listWithdrawals(params) {
  return request.get('/withdrawals', { params })
}

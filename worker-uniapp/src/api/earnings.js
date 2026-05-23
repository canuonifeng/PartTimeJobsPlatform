import request from './request'

export function getEarningsSummary() {
  return request({
    url: '/api/worker/earnings/summary',
    method: 'GET'
  })
}

export function getMyWithdrawals() {
  return request({
    url: '/api/worker/withdrawals/my',
    method: 'GET'
  })
}

export function createWithdrawal(data) {
  return request({
    url: '/api/worker/withdrawals',
    method: 'POST',
    data
  })
}

export function getEarningsTransactions(params) {
  return request({
    url: '/api/worker/earnings/transactions',
    method: 'GET',
    params
  })
}

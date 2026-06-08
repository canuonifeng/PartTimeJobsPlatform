import request from './request'

export function getEarningsSummary() {
  return request({
    url: '/api/earnings/summary',
    method: 'GET'
  })
}

export function getMyWithdrawals() {
  return request({
    url: '/api/withdrawals/my',
    method: 'GET'
  })
}

export function createWithdrawal(data) {
  return request({
    url: '/api/withdrawals',
    method: 'POST',
    data
  })
}

export function getEarningsTransactions(params) {
  return request({
    url: '/api/earnings/transactions',
    method: 'GET',
    data: params
  })
}

export function getWithdrawalMethods() {
  return request({
    url: '/api/withdrawal-methods',
    method: 'GET'
  })
}

export function getBankCards() {
  return request({
    url: '/api/bank-cards',
    method: 'GET'
  })
}

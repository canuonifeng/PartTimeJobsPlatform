import request from './request'

export function getEarningsSummary() {
  return request({
    url: '/earnings/summary',
    method: 'GET'
  })
}

export function getMyWithdrawals() {
  return request({
    url: '/withdrawals/my',
    method: 'GET'
  })
}

export function createWithdrawal(data) {
  return request({
    url: '/withdrawals',
    method: 'POST',
    data
  })
}

export function getEarningsTransactions(params) {
  return request({
    url: '/earnings/transactions',
    method: 'GET',
    data: params
  })
}

export function getWithdrawalMethods() {
  return request({
    url: '/withdrawal-methods',
    method: 'GET'
  })
}

export function getBankCards() {
  return request({
    url: '/bank-cards',
    method: 'GET'
  })
}

export function getReferralRewards() {
  return request({
    url: '/referral/rewards',
    method: 'GET'
  })
}

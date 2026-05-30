import request from './request'

export function getBalance() {
  return request.get('/enterprise/balance')
}

export function topUp(amount) {
  return request.post('/enterprise/balance/top-up', { amount })
}

export function getTransactions(params) {
  return request.get('/enterprise/balance/transactions', { params })
}

import request from './request'

export function getBalance() {
  return request.get('/balance')
}

export function topUp(amount) {
  return request.post('/balance/top-up', { amount })
}

export function getTransactions(params) {
  return request.get('/balance/transactions', { params })
}

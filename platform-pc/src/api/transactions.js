import request from './request'

export function listTransactions(params) {
  return request.post('/transactions/list', params)
}

export function getTransactionOverview(params) {
  return request.post('/transactions/overview', params)
}

export function getBalance(request) {
  return request('GET', '/enterprise/balance')
}

export function topUp(request, amount) {
  return request('POST', '/enterprise/balance/top-up', { amount })
}

export function getTransactions(request, params) {
  return request('GET', '/enterprise/balance/transactions', params)
}

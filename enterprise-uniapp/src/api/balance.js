export function getBalance(request) {
  return request('GET', '/balance')
}

export function topUp(request, amount) {
  return request('POST', '/balance/top-up', { amount })
}

export function getTransactions(request, params) {
  return request('GET', '/balance/transactions', params)
}

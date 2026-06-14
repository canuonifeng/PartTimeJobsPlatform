import request from './request'

export function getBankCard() {
  return request({ url: '/bank-card', method: 'GET' })
}

export function upsertBankCard(data) {
  return request({ url: '/bank-card', method: 'POST', data })
}

export function deleteBankCard() {
  return request({ url: '/bank-card/delete', method: 'POST', data: {} })
}

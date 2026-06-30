import request from './request'

export function listSettlements(params) {
  return request.post('/settlements/list', params)
}

export function getSettlementDetail(id) {
  return request.post('/settlements/detail', { id })
}

export function confirmSettlement(id) {
  return request.post('/settlements/confirm', { id })
}

export function cancelSettlement(id) {
  return request.post('/settlements/cancel', { id })
}

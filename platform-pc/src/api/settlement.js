import request from './request'

export function listSettlementBills(params) {
  return request.get('/admin/settlement/bills', { params })
}

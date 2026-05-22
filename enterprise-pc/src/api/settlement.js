import request from './request'

export function listBills(params) {
  return request.get('/settlement/bills', { params })
}



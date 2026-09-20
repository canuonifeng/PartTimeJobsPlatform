import request from './request'

export function listFaqs(params) {
  return request.post('/faqs/list', params || {})
}

export function getFaqDetail(id) {
  return request.post('/faqs/detail', { id })
}

export function createFaq(data) {
  return request.post('/faqs/create', data)
}

export function updateFaq(data) {
  return request.post('/faqs/update', data)
}

export function sortFaq(data) {
  return request.post('/faqs/sort', data)
}

export function deleteFaq(id) {
  return request.post('/faqs/delete', { id })
}

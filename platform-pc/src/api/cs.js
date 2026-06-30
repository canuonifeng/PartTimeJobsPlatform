import request from './request'

export function listSessions(params) {
  return request.post('/cs/sessions', params)
}

export function getSessionMessages(id) {
  return request.post('/cs/messages', { id })
}

export function sendMessage(data) {
  return request.post('/cs/send-message', data)
}

export function acceptSession(id) {
  return request.post('/cs/accept-session', { id })
}

export function closeSession(data) {
  return request.post('/cs/close-session', data)
}

export function listFaqs(params) {
  return request.post('/cs/faqs', params)
}

export function createFaq(data) {
  return request.post('/cs/faqs/create', data)
}

export function updateFaq(data) {
  return request.post('/cs/faqs/update', data)
}

export function deleteFaq(id) {
  return request.post('/cs/faqs/delete', { id })
}

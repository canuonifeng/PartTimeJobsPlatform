import request from './request'

export function listSessions(params) {
  return request.post('/cs/sessions', params || {})
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

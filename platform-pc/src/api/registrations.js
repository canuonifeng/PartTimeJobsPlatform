import request from './request'

export function getRegistrations() {
  return request.get('/registrations')
}

export function approveRegistration(id) {
  return request.put(`/registrations/${id}/approve`)
}

export function rejectRegistration(id, reason) {
  return request.put(`/registrations/${id}/reject`, { reason })
}

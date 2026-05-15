import request from './request'

export function getRegistrations(params) {
  return request.get('/registrations', { params })
}

export function getRegistration(id) {
  return request.get('/registrations', { params: { id } })
}

export function approveRegistration(id) {
  return request.put('/registrations/approve', null, { params: { id } })
}

export function rejectRegistration(id, data) {
  return request.put('/registrations/reject', data, { params: { id } })
}

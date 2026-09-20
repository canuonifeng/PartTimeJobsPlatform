import request from './request'

export function listComplaints(params) {
  return request.post('/complaints/list', params || {})
}

export function getComplaintDetail(id) {
  return request.post('/complaints/detail', { id })
}

export function handleComplaint(data) {
  return request.post('/complaints/handle', data)
}

export function arbitrateComplaint(data) {
  return request.post('/complaints/arbitrate', data)
}

export function closeComplaint(id) {
  return request.post('/complaints/close', { id })
}

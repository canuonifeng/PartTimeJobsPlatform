import request from './request'

export function listLocations() {
  return request.post('/locations/list')
}

export function createLocation(data) {
  return request.post('/locations/create', data)
}

export function updateLocation(data) {
  return request.post('/locations/update', data)
}

export function deleteLocation(id) {
  return request.post('/locations/delete', { id })
}

export function enableLocation(id) {
  return request.post('/locations/enable', { id })
}

export function disableLocation(id) {
  return request.post('/locations/disable', { id })
}

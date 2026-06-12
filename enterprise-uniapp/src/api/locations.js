import { request } from './request'

export function listLocations(params) {
  return request('POST', '/locations/list', params)
}

export function createLocation(data) {
  return request('POST', '/locations/create', data)
}

export function updateLocation(data) {
  return request('POST', '/locations/update', data)
}

export function deleteLocation(id) {
  return request('POST', '/locations/delete', { id })
}

export function enableLocation(id) {
  return request('POST', '/locations/enable', { id })
}

export function disableLocation(id) {
  return request('POST', '/locations/disable', { id })
}

import request from './request'

export function checkIn(data) {
  return request({
    url: '/api/worker/attendance/check-in',
    method: 'POST',
    data
  })
}

export function checkOut(data) {
  return request({
    url: '/api/worker/attendance/check-out',
    method: 'POST',
    data
  })
}

export function getMyAttendance() {
  return request({
    url: '/api/worker/attendance/my',
    method: 'GET'
  })
}

export function requestCorrection(data) {
  return request({
    url: '/api/worker/attendance/correction',
    method: 'POST',
    data
  })
}

export function getCorrectionStatus(params) {
  return request({
    url: '/api/worker/attendance/correction/status',
    method: 'GET',
    params
  })
}

import request from './request'

export function checkIn(data) {
  return request({
    url: '/api/attendance/check-in',
    method: 'POST',
    data
  })
}

export function checkOut(data) {
  return request({
    url: '/api/attendance/check-out',
    method: 'POST',
    data
  })
}

export function getMyAttendance() {
  return request({
    url: '/api/attendance/my',
    method: 'GET'
  })
}

export function submitCorrection(data) {
  return request({
    url: '/api/attendance/correction',
    method: 'POST',
    data
  })
}

export function getCorrectionStatus(params) {
  return request({
    url: '/api/attendance/correction/status',
    method: 'GET',
    data: params
  })
}

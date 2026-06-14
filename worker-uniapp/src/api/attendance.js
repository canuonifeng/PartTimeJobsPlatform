import request from './request'

export function checkIn(data) {
  return request({
    url: '/attendance/check-in',
    method: 'POST',
    data
  })
}

export function checkOut(data) {
  return request({
    url: '/attendance/check-out',
    method: 'POST',
    data
  })
}

export function getMyAttendance(params) {
  return request({
    url: '/attendance/my',
    method: 'GET',
    data: params
  })
}

export function submitCorrection(data) {
  return request({
    url: '/attendance/correction',
    method: 'POST',
    data
  })
}

export function getCorrectionStatus(params) {
  return request({
    url: '/attendance/correction/status',
    method: 'GET',
    data: params
  })
}

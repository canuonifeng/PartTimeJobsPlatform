import request from './request'

export function getMyAttendance(params) {
  return request({
    url: '/api/attendance/my',
    method: 'GET',
    data: params
  })
}

export function getMyWithdrawals(params) {
  return request({
    url: '/api/withdrawals/my',
    method: 'GET',
    data: params
  })
}

export function createWithdrawal(data) {
  return request({
    url: '/api/withdrawals',
    method: 'POST',
    data
  })
}

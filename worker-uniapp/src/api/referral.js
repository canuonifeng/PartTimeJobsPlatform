import request from './request'

export function getReferralLink() {
  return request({ url: '/api/referral/link', method: 'GET' })
}

export function getReferralPoster() {
  return request({ url: '/api/referral/poster', method: 'GET' })
}

export function getReferralStats() {
  return request({ url: '/api/referral/stats', method: 'GET' })
}

export function getReferees(params) {
  return request({ url: '/api/referral/referees', method: 'GET', data: params })
}

export function getReferralRewards(params) {
  return request({ url: '/api/referral/rewards', method: 'GET', data: params })
}

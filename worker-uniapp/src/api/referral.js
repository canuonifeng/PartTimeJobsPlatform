import request from './request'

export function getReferralLink() {
  return request({ url: '/referral/link', method: 'GET' })
}

export function getReferralPoster() {
  return request({ url: '/referral/poster', method: 'GET' })
}

export function getReferralStats() {
  return request({ url: '/referral/stats', method: 'GET' })
}

export function getReferees(params) {
  return request({ url: '/referral/referees', method: 'GET', data: params })
}

export function getReferralRewards(params) {
  return request({ url: '/referral/rewards', method: 'GET', data: params })
}

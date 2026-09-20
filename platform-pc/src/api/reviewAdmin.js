import request from './request'

export function listReviews(params) {
  return request.post('/reviews/list', params || {})
}

export function getReviewDetail(id) {
  return request.post('/reviews/detail', { id })
}

export function markReviewViolation(data) {
  return request.post('/reviews/mark-violation', data)
}

export function deleteReview(id) {
  return request.post('/reviews/delete', { id })
}

export function adjustCreditScore(data) {
  return request.post('/reviews/adjust-credit', data)
}

import request from './request'

export function listReviews(params) {
  return request.post('/reviews/list', params)
}

export function getReviewDetail(id) {
  return request.post('/reviews/detail', { id })
}

export function markReviewViolation(id) {
  return request.post('/reviews/mark-violation', { id })
}

export function deleteReview(id) {
  return request.post('/reviews/delete', { id })
}

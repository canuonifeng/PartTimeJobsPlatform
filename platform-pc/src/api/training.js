import request from './request'

// ===== 技能认证管理 =====
export function getTrainingCertifications() {
  return request.get('/training/certifications')
}

export function createTrainingCertification(data) {
  return request.post('/training/certifications', data)
}

export function updateTrainingCertification(data) {
  return request.post('/training/certifications/update', data)
}

export function toggleTrainingCertification(id, status) {
  return request.post('/training/certifications/toggle', { id, status })
}

// ===== 培训课程管理 =====
export function getTrainingCourses() {
  return request.get('/training/courses')
}

export function createTrainingCourse(data) {
  return request.post('/training/courses', data)
}

export function updateTrainingCourse(data) {
  return request.post('/training/courses/update', data)
}

export function publishTrainingCourse(id) {
  return request.post('/training/courses/publish', { id })
}

export function offlineTrainingCourse(id) {
  return request.post('/training/courses/offline', { id })
}

export function deleteTrainingCourse(id) {
  return request.post('/training/courses/delete', { id })
}

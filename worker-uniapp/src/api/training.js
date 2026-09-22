import request from './request'

export function getTrainingCourses() {
  return request({ url: '/training/courses', method: 'GET' })
}

export function courseDetail(id) {
  return request({ url: '/training/courses/detail', method: 'GET', data: { id } })
}

export function startLesson(lessonId) {
  return request({ url: '/training/lessons/start', method: 'POST', data: { lessonId } })
}

export function reportProgress(lessonId, progress) {
  return request({ url: '/training/lessons/progress', method: 'POST', data: { lessonId, progress } })
}

export function completeLesson(lessonId) {
  return request({ url: '/training/lessons/complete', method: 'POST', data: { lessonId } })
}

export function submitExam(lessonId, answers) {
  return request({ url: '/training/lessons/exam/submit', method: 'POST', data: { lessonId, answers } })
}

export function getMyCertifications() {
  return request({ url: '/training/certifications/my', method: 'GET' })
}

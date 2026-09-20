import request from './request'

// 培训课程列表
export function getTrainingCourses() {
  return request({ url: '/training/courses', method: 'GET' })
}

// 课程详情
export function getTrainingCourseDetail(id) {
  return request({ url: '/training/courses/detail', method: 'GET', data: { id } })
}

// 开始学习
export function startTrainingCourse(id) {
  return request({ url: '/training/courses/start', method: 'POST', data: { courseId: id } })
}

// 提交考试
export function submitExam(id, answers) {
  return request({ url: '/training/courses/exam', method: 'POST', data: { courseId: id, answers } })
}

// 我的技能认证
export function getMyCertifications() {
  return request({ url: '/training/certifications/my', method: 'GET' })
}

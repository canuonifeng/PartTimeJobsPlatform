import axios from 'axios'
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

// ===== 题库管理 =====
export function questionBankList() {
  return request.post('/training/question-banks/list')
}

export function questionBankCreate(data) {
  return request.post('/training/question-banks/create', data)
}

export function questionBankUpdate(data) {
  return request.post('/training/question-banks/update', data)
}

export function questionBankToggle(id) {
  return request.post('/training/question-banks/toggle', { id })
}

// ===== 题目管理 =====
export function questionList(data) {
  return request.post('/training/question-banks/questions/list', data)
}

export function questionCreate(data) {
  return request.post('/training/question-banks/questions/create', data)
}

export function questionUpdate(data) {
  return request.post('/training/question-banks/questions/update', data)
}

export function questionDelete(id) {
  return request.post('/training/question-banks/questions/delete', { id })
}

export function questionPublish(id) {
  return request.post('/training/question-banks/questions/publish', { id })
}

export function questionOffline(id) {
  return request.post('/training/question-banks/questions/offline', { id })
}

// ===== 课时管理 =====
export function lessonList(courseId) {
  return request.post('/training/lessons/list', { courseId })
}

export function lessonCreate(data) {
  return request.post('/training/lessons/create', data)
}

export function lessonUpdate(data) {
  return request.post('/training/lessons/update', data)
}

export function lessonDelete(id) {
  return request.post('/training/lessons/delete', { id })
}

export function lessonPublish(id) {
  return request.post('/training/lessons/publish', { id })
}

export function lessonOffline(id) {
  return request.post('/training/lessons/offline', { id })
}

export function lessonSort(data) {
  return request.post('/training/lessons/sort', data)
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
export const adminUploadUrl = `${API_BASE_URL}/admin/files/upload`

export function getUploadHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getUploadUrl(response) {
  return response?.data?.url || response?.url || ''
}

function authConfig() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getSts(biz) {
  const res = await axios.post(`${API_BASE_URL}/admin/files/sts`, null, {
    params: { biz },
    headers: authConfig()
  })
  return res.data
}

export function getSignedUrl(key) {
  return request.get('/files/signed-url', { params: { key } })
}

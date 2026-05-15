import request from './request'

export function getCategories() {
  return request.get('/admin/job-categories')
}

export function createCategory(data) {
  return request.post('/admin/job-categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/job-categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/job-categories/${id}`)
}

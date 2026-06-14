import request from './request'

export function getCategories() {
  return request.get('/job-categories')
}

export function createCategory(data) {
  return request.post('/job-categories', data)
}

export function updateCategory(id, data) {
  return request.post('/job-categories/update', { ...data, id })
}

export function deleteCategory(id) {
  return request.post('/job-categories/delete', { id })
}

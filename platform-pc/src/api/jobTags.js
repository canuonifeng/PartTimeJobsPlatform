import request from './request'

export function getJobTags() {
  return request.get('/admin/job-tags')
}

export function createTagGroup(data) {
  return request.post('/admin/job-tag-groups', data)
}

export function updateTagGroup(id, data) {
  return request.put('/admin/job-tag-groups', data, { params: { id } })
}

export function deleteTagGroup(id) {
  return request.delete('/admin/job-tag-groups', { params: { id } })
}

export function createTag(data) {
  return request.post('/admin/job-tags', data)
}

export function updateTag(id, data) {
  return request.put('/admin/job-tags', data, { params: { id } })
}

export function deleteTag(id) {
  return request.delete('/admin/job-tags', { params: { id } })
}

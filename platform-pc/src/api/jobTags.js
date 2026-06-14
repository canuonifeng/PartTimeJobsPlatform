import request from './request'

export function getJobTags() {
  return request.get('/job-tags')
}

export function createTagGroup(data) {
  return request.post('/job-tag-groups', data)
}

export function updateTagGroup(id, data) {
  return request.post('/job-tag-groups/update', { ...data, id })
}

export function deleteTagGroup(id) {
  return request.post('/job-tag-groups/delete', { id })
}

export function createTag(data) {
  return request.post('/job-tags', data)
}

export function updateTag(id, data) {
  return request.post('/job-tags/update', { ...data, id })
}

export function deleteTag(id) {
  return request.post('/job-tags/delete', { id })
}

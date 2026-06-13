import { request } from './request'

export function getOperationDashboard() {
  return request('GET', '/operations/dashboard')
}

export function getOperationProcess() {
  return request('GET', '/operations/process')
}

export function getOperationTodos(params = {}) {
  const query = []
  if (params.type) query.push(`type=${encodeURIComponent(params.type)}`)
  if (params.page != null) query.push(`page=${params.page}`)
  if (params.pageSize != null) query.push(`pageSize=${params.pageSize}`)
  return request('GET', `/operations/todos${query.length ? `?${query.join('&')}` : ''}`)
}

export function executeOperationTodoAction(todoId, action) {
  return request('POST', `/operations/todos/${encodeURIComponent(todoId)}/actions`, { action })
}

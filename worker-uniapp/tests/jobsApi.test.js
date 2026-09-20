import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../src/api/request', () => ({
  default: vi.fn()
}))

import request from '../src/api/request'
import {
  getJobs,
  getAnnotationJobDetail,
  getTaskOrders,
  grabTaskOrder
} from '../src/api/jobs'

describe('worker-uniapp 标注任务 api 层', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    request.mockResolvedValue({})
  })

  it('getJobs 透传 taskType 参数（标注 tab 筛选）', async () => {
    await getJobs({ taskType: 'ANNOTATION', page: 1 })
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/jobs',
      method: 'GET',
      data: { taskType: 'ANNOTATION', page: 1 }
    }))
  })

  it('getAnnotationJobDetail 固定携带 ANNOTATION 类型', async () => {
    await getAnnotationJobDetail(42)
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/jobs/detail',
      method: 'GET',
      data: { id: 42, taskType: 'ANNOTATION' }
    }))
  })

  it('getTaskOrders 请求我的任务单列表', async () => {
    await getTaskOrders({ status: 'SUBMITTED' })
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/jobs/task-orders/my',
      method: 'GET',
      data: { status: 'SUBMITTED' }
    }))
  })

  it('grabTaskOrder 抢单请求带 jobId 和参数', async () => {
    await grabTaskOrder(7, {})
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/jobs/task-orders/grab',
      method: 'POST',
      data: { jobId: 7 }
    }))
  })
})

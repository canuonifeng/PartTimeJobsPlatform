import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import JobList from '../src/views/jobs/JobList.vue'

vi.mock('../src/api/job', () => ({
  listJobs: vi.fn(),
  publishJob: vi.fn(),
  closeJob: vi.fn(),
  reopenJob: vi.fn(),
  getJobShareLink: vi.fn()
}))

import { listJobs } from '../src/api/job'

function mountJobList() {
  return mount(JobList, {
    global: {
      plugins: [ElementPlus],
      mocks: {
        $router: { push: vi.fn() }
      }
    }
  })
}

describe('JobList 任务类型筛选', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listJobs.mockResolvedValue({
      records: [
        { id: 1, title: '搬运工', taskType: 'WORK', status: 'PUBLISHED', categoryName: '物流' },
        { id: 2, title: '图片标注', taskType: 'ANNOTATION', status: 'PUBLISHED', categoryName: '数据' }
      ],
      total: 2
    })
  })

  it('渲染任务类型筛选下拉框（零工/标注）', async () => {
    const wrapper = mountJobList()
    await wrapper.vm.$nextTick()
    const labels = wrapper.findAll('.el-select .el-input__inner').map(n => n.element.placeholder || '')
    // 状态 + 任务类型两个下拉
    expect(wrapper.find('form').text()).toContain('任务类型')
  })

  it('搜索时把 taskType 传给 listJobs', async () => {
    const wrapper = mountJobList()
    await wrapper.vm.$nextTick()
    wrapper.vm.searchForm.taskType = 'ANNOTATION'
    await wrapper.vm.handleSearch()
    expect(listJobs).toHaveBeenCalledWith(
      expect.objectContaining({ taskType: 'ANNOTATION' })
    )
  })

  it('重置时清空 taskType', async () => {
    const wrapper = mountJobList()
    await wrapper.vm.$nextTick()
    wrapper.vm.searchForm.taskType = 'ANNOTATION'
    await wrapper.vm.handleReset()
    expect(listJobs).toHaveBeenCalledWith(
      expect.objectContaining({ taskType: '' })
    )
  })

  it('表格展示任务类型列', async () => {
    const wrapper = mountJobList()
    await wrapper.vm.$nextTick()
    await wrapper.vm.fetchData()
    await wrapper.vm.$nextTick()
    const text = wrapper.find('.el-table').text()
    expect(text).toContain('任务类型')
    expect(text).toContain('零工')
    expect(text).toContain('标注')
  })
})

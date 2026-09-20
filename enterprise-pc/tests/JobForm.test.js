import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import JobForm from '../src/views/jobs/JobForm.vue'

vi.mock('../src/api/job', () => ({
  getJob: vi.fn().mockResolvedValue(null),
  createJob: vi.fn(),
  updateJob: vi.fn(),
  getJobTags: vi.fn().mockResolvedValue([])
}))
vi.mock('../src/api/location', () => ({
  listLocations: vi.fn().mockResolvedValue({ records: [] })
}))
vi.mock('../src/api/template', () => ({
  listTemplates: vi.fn().mockResolvedValue({ records: [] })
}))
vi.mock('../src/api/upload', () => ({
  enterpriseUploadUrl: '',
  getUploadHeaders: vi.fn().mockReturnValue({}),
  getUploadUrl: vi.fn().mockReturnValue('')
}))
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ query: {}, params: {} })
}))

import { createJob } from '../src/api/job'

function mountJobForm() {
  return mount(JobForm, {
    global: {
      plugins: [ElementPlus]
    }
  })
}

describe('JobForm 标注任务发布流程', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    createJob.mockResolvedValue({ id: 9 })
  })

  it('默认是零工任务，不显示标注计价字段', async () => {
    const wrapper = mountJobForm()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.form.taskType).toBe('WORK')
    const formText = wrapper.find('form').text()
    expect(formText).not.toContain('计价方式')
    expect(formText).not.toContain('任务总量')
  })

  it('切换为标注任务后显示计价方式/单价/任务总量', async () => {
    const wrapper = mountJobForm()
    await wrapper.vm.$nextTick()
    wrapper.vm.form.taskType = 'ANNOTATION'
    await wrapper.vm.$nextTick()
    const formText = wrapper.find('form').text()
    expect(formText).toContain('计价方式')
    expect(formText).toContain('单价')
    expect(formText).toContain('任务总量')
  })

  it('提交标注任务时只携带标注计价字段', async () => {
    const wrapper = mountJobForm()
    await wrapper.vm.$nextTick()
    wrapper.vm.form.taskType = 'ANNOTATION'
    wrapper.vm.form.pricingMode = 'PER_ITEM'
    wrapper.vm.form.pricePerUnit = 0.5
    wrapper.vm.form.totalItems = 1000
    wrapper.vm.form.title = '图片标注'
    wrapper.vm.form.category = 'tech'
    wrapper.vm.form.description = '标注描述'
    await wrapper.vm.handleSubmit()
    expect(createJob).toHaveBeenCalledWith(
      expect.objectContaining({
        taskType: 'ANNOTATION',
        pricingMode: 'PER_ITEM',
        pricePerUnit: 0.5,
        totalItems: 1000
      })
    )
  })
})

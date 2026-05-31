<template>
  <view class="edit-page">
    <view class="avatar-section" @click="changeAvatar">
      <image class="avatar" :src="form.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <view class="form-group">
      <text class="form-label">姓名</text>
      <input class="form-input" v-model="form.name" placeholder="请输入姓名" />
    </view>

    <view class="form-group">
      <text class="form-label">手机号</text>
      <input class="form-input" v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" />
    </view>

    <view class="form-group">
      <text class="form-label">性别</text>
      <view class="gender-row">
        <view
          v-for="opt in genderOptions"
          :key="opt.value"
          class="gender-chip"
          :class="{ selected: form.gender === opt.value }"
          @click="form.gender = opt.value"
        >
          {{ opt.label }}
        </view>
      </view>
    </view>

    <view class="form-group">
      <text class="form-label">出生日期</text>
      <picker mode="date" :value="form.birthday" :end="todayStr" @change="onBirthdayChange">
        <view class="form-input picker-value">{{ form.birthday || '请选择出生日期' }}</view>
      </picker>
    </view>

    <view class="form-group">
      <text class="form-label">技能标签</text>
      <view class="skill-input-area">
        <view class="skill-tags">
          <view v-for="(skill, i) in form.skills" :key="i" class="skill-tag">
            <text>{{ skill }}</text>
            <text class="tag-remove" @click="removeSkill(i)">x</text>
          </view>
        </view>
        <view class="skill-add">
          <input class="skill-input" v-model="newSkill" placeholder="输入技能，按确认添加" @confirm="addSkill" />
        </view>
      </view>
    </view>

    <view class="form-group">
      <text class="form-label">可工作日期</text>
      <view class="day-checkboxes">
        <view
          v-for="day in weekDays"
          :key="day.value"
          class="day-chip"
          :class="{ selected: form.availableDays?.includes(day.value) }"
          @click="toggleDay(day.value)"
        >
          {{ day.label }}
        </view>
      </view>
    </view>

    <view class="save-area">
      <button class="save-btn" type="primary" @click="handleSave" :loading="saving">保存</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getProfile, updateProfile } from '@/api/profile'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()

const weekDays = [
  { label: '周一', value: 'Monday' },
  { label: '周二', value: 'Tuesday' },
  { label: '周三', value: 'Wednesday' },
  { label: '周四', value: 'Thursday' },
  { label: '周五', value: 'Friday' },
  { label: '周六', value: 'Saturday' },
  { label: '周日', value: 'Sunday' }
]

const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' },
  { label: '其他', value: 'OTHER' }
]

const todayStr = new Date().toISOString().slice(0, 10)

const form = reactive({
  name: '',
  phone: '',
  avatar: '',
  gender: '',
  birthday: '',
  skills: [] as string[],
  availableDays: [] as string[]
})

function onBirthdayChange(e: any) {
  form.birthday = e.detail.value
}

const newSkill = ref('')
const saving = ref(false)

function addSkill() {
  const val = newSkill.value.trim()
  if (val && !form.skills.includes(val)) {
    form.skills.push(val)
  }
  newSkill.value = ''
}

function removeSkill(index: number) {
  form.skills.splice(index, 1)
}

function toggleDay(day: string) {
  const idx = form.availableDays.indexOf(day)
  if (idx >= 0) {
    form.availableDays.splice(idx, 1)
  } else {
    form.availableDays.push(day)
  }
}

function changeAvatar() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempPath = res.tempFilePaths[0]
      form.avatar = tempPath
    }
  })
}

async function handleSave() {
  if (!form.name) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateProfile({ ...form })
    authStore.setWorkerInfo({ ...form })
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.navigateBack()
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res: any = await getProfile()
    if (res) {
      form.name = res.name || ''
      form.phone = res.phone || ''
      form.avatar = res.avatar || res.avatarUrl || ''
      form.gender = res.gender || ''
      form.birthday = res.birthday || ''
      form.skills = res.skills || []
      form.availableDays = res.availableDays || []
    }
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.edit-page {
  padding: 30rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  margin-bottom: 16rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: #999;
}

.form-group {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  width: 100%;
  height: 72rpx;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f0f0f0;
}

.picker-value {
  line-height: 72rpx;
  color: #333;
}

.gender-row {
  display: flex;
  gap: 16rpx;
}

.gender-chip {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 8rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
}

.gender-chip.selected {
  background: #07c160;
  color: #fff;
}

.skill-input-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  width: 100%;
}

.skill-tag {
  display: flex;
  align-items: center;
  padding: 8rpx 20rpx;
  background: #e8f8ee;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #07c160;
}

.tag-remove {
  margin-left: 8rpx;
  color: #ccc;
  font-size: 24rpx;
}

.skill-add {
  width: 100%;
  margin-top: 12rpx;
}

.skill-input {
  width: 100%;
  height: 60rpx;
  font-size: 26rpx;
  color: #333;
  border-bottom: 1rpx solid #f0f0f0;
}

.day-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.day-chip {
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
}

.day-chip.selected {
  background: #07c160;
  color: #fff;
}

.save-area {
  padding: 20rpx 0;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
}
</style>

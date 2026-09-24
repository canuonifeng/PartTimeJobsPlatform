<template>
  <view class="edit-page">
    <view class="avatar-section" @click="changeAvatar">
      <image class="avatar" :src="previewAvatar || form.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="avatar-text">
        <text class="avatar-title">个人资料</text>
        <text class="avatar-tip">点击更换头像</text>
      </view>
    </view>

    <view class="form-card">
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
            v-for="(opt, index) in genderOptions"
            :key="opt.value"
            class="gender-chip"
            :class="[{ selected: form.gender === opt.value }, index < genderOptions.length - 1 ? 'chip-space' : '']"
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
    </view>

    <view class="form-card">
      <view class="form-group">
        <text class="form-label">技能标签</text>
        <view class="skill-tags">
          <view v-for="(skill, i) in form.skills" :key="i" class="skill-tag">
            <text>{{ skill }}</text>
            <text class="tag-remove" @click="removeSkill(i)">x</text>
          </view>
        </view>
        <input class="skill-input" v-model="newSkill" placeholder="输入技能，按确认添加" @confirm="addSkill" />
      </view>

      <view class="form-group last-group">
        <text class="form-label">可工作日期</text>
        <view class="day-checkboxes">
          <view
            v-for="(day, index) in weekDays"
            :key="day.value"
            class="day-chip"
            :class="[{ selected: form.availableDays.includes(day.value) }, index % 3 !== 2 ? 'day-space' : '']"
            @click="toggleDay(day.value)"
          >
            {{ day.label }}
          </view>
        </view>
      </view>
    </view>

    <view class="save-area">
      <button class="save-btn" type="primary" :loading="saving" :disabled="saving" @click="handleSave">
        {{ saving ? '保存中' : '保存' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getProfile, updateProfile } from '@/api/profile'
import { uploadToOss } from '@/utils/ossUpload'
import { useAuthStore } from '@/store'

const previewAvatar = ref('')
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
      previewAvatar.value = res.tempFilePaths[0]
    }
  })
}

async function handleSave() {
  if (saving.value) return
  const name = form.name.trim()
  const phone = form.phone.trim()
  if (!name) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  if (phone && !/^1\d{10}$/.test(phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  saving.value = true
  if (previewAvatar.value) {
    try {
      const uploadRes = await uploadToOss(previewAvatar.value, 'avatar')
      const avatarUrl = uploadRes.url || uploadRes.key
      const payload = {
        name,
        phone,
        avatar: avatarUrl,
        gender: form.gender,
        birthday: form.birthday,
        skills: [...form.skills],
        availableDays: [...form.availableDays]
      }
      await updateProfile(payload)
      authStore.setWorkerInfo(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
      uni.navigateBack()
    } catch (e: any) {
      uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
    } finally {
      saving.value = false
    }
    return
  }
  const payload = {
    name,
    phone,
    avatar: previewAvatar.value || form.avatar,
    gender: form.gender,
    birthday: form.birthday,
    skills: [...form.skills],
    availableDays: [...form.availableDays]
  }
  try {
    await updateProfile(payload)
    authStore.setWorkerInfo(payload)
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.navigateBack()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
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
      form.skills = Array.isArray(res.skills) ? res.skills : []
      form.availableDays = Array.isArray(res.availableDays) ? res.availableDays : []
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '资料加载失败', icon: 'none' })
  }
})
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f5f7fa;
  box-sizing: border-box;
}

.avatar-section {
  display: flex;
  align-items: center;
  padding: 34rpx;
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0, 0, 0, 0.05);
}

.avatar {
  width: 132rpx;
  height: 132rpx;
  border-radius: 66rpx;
  margin-right: 28rpx;
  background: #f1f1f1;
}

.avatar-text {
  flex: 1;
}

.avatar-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #222;
  margin-bottom: 12rpx;
}

.avatar-tip {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 18rpx rgba(0, 0, 0, 0.05);
}

.form-group {
  padding-bottom: 28rpx;
  margin-bottom: 28rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.last-group {
  padding-bottom: 0;
  margin-bottom: 0;
  border-bottom: none;
}

.form-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  width: 100%;
  height: 76rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  box-sizing: border-box;
}

.picker-value {
  line-height: 76rpx;
  color: #333;
}

.gender-row {
  display: flex;
}

.gender-chip {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 12rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
}

.chip-space {
  margin-right: 16rpx;
}

.gender-chip.selected {
  background: #07c160;
  color: #fff;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16rpx;
}

.skill-tag {
  display: flex;
  align-items: center;
  padding: 10rpx 20rpx;
  margin-right: 12rpx;
  margin-bottom: 12rpx;
  background: #e8f8ee;
  border-radius: 28rpx;
  font-size: 24rpx;
  color: #07c160;
}

.tag-remove {
  margin-left: 10rpx;
  color: #9ca3af;
  font-size: 24rpx;
}

.skill-input {
  width: 100%;
  height: 72rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  box-sizing: border-box;
}

.day-checkboxes {
  display: flex;
  flex-wrap: wrap;
}

.day-chip {
  width: 30%;
  text-align: center;
  padding: 16rpx 0;
  margin-bottom: 14rpx;
  border-radius: 40rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
  box-sizing: border-box;
}

.day-space {
  margin-right: 5%;
}

.day-chip.selected {
  background: #07c160;
  color: #fff;
}

.save-area {
  padding: 10rpx 0 20rpx;
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

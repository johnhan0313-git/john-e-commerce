<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>卖家登录 / 注册</h2>
      <p class="sub">邮箱验证码登录，未注册将自动开通账号（无需密码）；入驻审核请登录后提交资料</p>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="邮箱" required>
          <el-input v-model="email" type="email" placeholder="seller@example.com" autocomplete="username" />
        </el-form-item>
        <el-form-item label="验证码" required>
          <div class="code-row">
            <el-input
              v-model="code"
              placeholder="6 位验证码"
              maxlength="6"
              autocomplete="one-time-code"
            />
            <el-button :disabled="cooldown > 0 || sending || !email.trim()" @click="sendCode">
              {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-alert v-if="err" :title="err" type="error" show-icon :closable="false" class="mb" />
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          登录 / 注册
        </el-button>
        <p class="hint">本地开发可用固定码 123456（app.auth.fixed-code）</p>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'

const email = ref('')
const code = ref('')
const err = ref('')
const loading = ref(false)
const sending = ref(false)
const cooldown = ref(0)
let timer: number | undefined

const auth = useAuthStore()
const merchant = useMerchantStore()
const router = useRouter()

async function sendCode() {
  err.value = ''
  if (!email.value.trim()) {
    err.value = '请先填写邮箱'
    return
  }
  sending.value = true
  try {
    await auth.sendCode(email.value.trim())
    ElMessage.success('验证码已发送')
    cooldown.value = 60
    timer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0 && timer) {
        clearInterval(timer)
        timer = undefined
      }
    }, 1000)
  } catch (e: any) {
    err.value = e.response?.data?.message || '发送失败'
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  err.value = ''
  loading.value = true
  try {
    await auth.login(email.value.trim(), code.value.trim())
    merchant.clear()
    await merchant.fetchMe()
    ElMessage.success('登录成功')
    router.push(merchant.isApproved() ? '/dashboard' : '/apply')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(160deg, #f0f4f8, #ffffff 45%, #eef2f7);
}
.login-card { width: min(100% - 32px, 400px); }
.sub { margin: -8px 0 16px; font-size: 13px; color: #909399; line-height: 1.5; }
.code-row { display: flex; gap: 8px; width: 100%; }
.mb { margin-bottom: 16px; }
.hint { margin: 12px 0 0; font-size: 12px; color: #909399; }
h2 { margin: 0 0 20px; }
</style>

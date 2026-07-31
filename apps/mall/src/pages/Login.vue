<template>
  <div class="login-page">
    <div class="card login-card">
      <h1>登录 / 注册</h1>
      <p class="muted">邮箱验证码登录，未注册将自动开通买家账号（无需密码）</p>
      <form @submit.prevent="onSubmit">
        <label class="field">
          <span>邮箱</span>
          <input v-model="email" type="email" placeholder="you@example.com" autocomplete="username" required />
        </label>
        <label class="field">
          <span>验证码</span>
          <div class="code-row">
            <input
              v-model="code"
              type="text"
              inputmode="numeric"
              maxlength="6"
              placeholder="6 位验证码"
              autocomplete="one-time-code"
              required
            />
            <button class="btn btn-ghost" type="button" :disabled="cooldown > 0 || sending || !email.trim()" @click="sendCode">
              {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
            </button>
          </div>
        </label>
        <p v-if="err" class="error">{{ err }}</p>
        <button class="btn btn-block" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录 / 注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'
import { toast } from '@/utils/toast'

const email = ref('')
const code = ref('')
const err = ref('')
const loading = ref(false)
const sending = ref(false)
const cooldown = ref(0)
let timer: number | undefined

const auth = useAuthStore()
const modules = useModulesStore()
const router = useRouter()
const route = useRoute()

async function sendCode() {
  err.value = ''
  if (!email.value.trim()) {
    err.value = '请先填写邮箱'
    return
  }
  sending.value = true
  try {
    await auth.sendEmailCode(email.value.trim())
    toast('验证码已发送', 'success')
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
    await modules.fetch()
    toast('登录成功', 'success')
    router.replace((route.query.redirect as string) || '/')
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
.login-page {
  min-height: calc(100vh - var(--nav-h) - 80px);
  display: grid;
  place-items: center;
}

.login-card {
  width: min(100%, 400px);
  padding: var(--space-6);
}

.code-row {
  display: flex;
  gap: 8px;
}

.code-row input {
  flex: 1;
}

.error {
  color: var(--color-danger);
  margin: 0 0 var(--space-4);
  font-size: 14px;
}
</style>

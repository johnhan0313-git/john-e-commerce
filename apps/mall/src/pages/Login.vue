<template>
  <div class="login-page">
    <div class="card login-card">
      <h1>登录</h1>
      <p class="muted">使用手机号登录演示商城</p>
      <form @submit.prevent="onSubmit">
        <label class="field">
          <span>手机号</span>
          <input v-model="phone" type="tel" placeholder="13800000000" autocomplete="username" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" placeholder="密码" autocomplete="current-password" />
        </label>
        <p v-if="err" class="error">{{ err }}</p>
        <button class="btn btn-block" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'
import { toast } from '@/utils/toast'

const phone = ref('13800000000')
const password = ref('admin123')
const err = ref('')
const loading = ref(false)
const auth = useAuthStore()
const modules = useModulesStore()
const router = useRouter()
const route = useRoute()

async function onSubmit() {
  err.value = ''
  loading.value = true
  try {
    await auth.login(phone.value.trim(), password.value)
    await modules.fetch()
    toast('登录成功', 'success')
    router.replace((route.query.redirect as string) || '/')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
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

.error {
  color: var(--color-danger);
  margin: 0 0 var(--space-4);
  font-size: 14px;
}
</style>

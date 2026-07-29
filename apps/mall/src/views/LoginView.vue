<template>
  <div>
    <h2>登录</h2>
    <form @submit.prevent="handleLogin">
      <div><input v-model="username" placeholder="用户名" /></div>
      <div><input v-model="password" type="password" placeholder="密码" /></div>
      <button type="submit">登录</button>
    </form>
    <p v-if="error">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { authStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()

async function handleLogin() {
  try {
    await authStore.login(username.value, password.value)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || '登录失败'
  }
}
</script>

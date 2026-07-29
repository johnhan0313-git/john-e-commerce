<template>
  <div style="max-width:300px; margin:100px auto">
    <h2>管理后台登录</h2>
    <input v-model="phone" placeholder="手机号" /><br/>
    <input v-model="password" type="password" placeholder="密码" /><br/>
    <button @click="login">登录</button>
    <p v-if="err" style="color:red">{{ err }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import client from '@/api/client'

const phone = ref('')
const password = ref('')
const err = ref('')
const router = useRouter()

async function login() {
  try {
    const res: any = await client.post('/auth/login', { phone: phone.value, password: password.value })
    localStorage.setItem('admin_token', res.data.token)
    router.push('/dashboard')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  }
}
</script>

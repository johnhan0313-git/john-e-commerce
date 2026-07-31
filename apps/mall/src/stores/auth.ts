import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { LoginVO, R, UserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function sendEmailCode(email: string) {
    await client.post('/auth/email-code', { email })
  }

  async function login(email: string, code: string) {
    const res = await client.post('/auth/login', { email, code }) as R<LoginVO>
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, sendEmailCode, login, logout }
})

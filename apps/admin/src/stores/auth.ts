import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { LoginVO, R } from '@/types'

const PORTAL = 'admin'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const isLoggedIn = computed(() => !!token.value)

  async function sendEmailCode(email: string) {
    await client.post('/auth/email-code', { email, portal: PORTAL })
  }

  async function login(email: string, code: string) {
    const res = await client.post('/auth/login', { email, code, portal: PORTAL }) as R<LoginVO>
    token.value = res.data.token
    localStorage.setItem('admin_token', token.value)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem('admin_token')
  }

  return { token, isLoggedIn, sendEmailCode, login, logout }
})

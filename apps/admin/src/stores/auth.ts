import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { LoginVO, R } from '@/types'

const PORTAL = 'admin'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const email = ref(localStorage.getItem('admin_email') || '')
  const isLoggedIn = computed(() => !!token.value)

  async function sendEmailCode(emailAddr: string) {
    await client.post('/auth/email-code', { email: emailAddr, portal: PORTAL })
  }

  async function login(emailAddr: string, code: string) {
    const res = await client.post('/auth/login', { email: emailAddr, code, portal: PORTAL }) as R<LoginVO>
    token.value = res.data.token
    email.value = emailAddr
    localStorage.setItem('admin_token', token.value)
    localStorage.setItem('admin_email', emailAddr)
  }

  function logout() {
    token.value = ''
    email.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_email')
  }

  return { token, email, isLoggedIn, sendEmailCode, login, logout }
})

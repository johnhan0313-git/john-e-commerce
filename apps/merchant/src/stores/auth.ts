import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import client from '@/api/client'
import type { R } from '@/types'

const TOKEN_KEY = 'merchant_token'
const PORTAL = 'merchant'

const EMAIL_KEY = 'merchant_email'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const email = ref(localStorage.getItem(EMAIL_KEY) || '')

  const isLoggedIn = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem(TOKEN_KEY, t)
  }

  function logout() {
    token.value = ''
    email.value = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EMAIL_KEY)
  }

  async function sendCode(emailAddr: string) {
    return client.post('/auth/email-code', { email: emailAddr, portal: PORTAL }) as Promise<R<unknown>>
  }

  async function login(emailAddr: string, code: string) {
    const res = await client.post('/auth/login', { email: emailAddr, code, portal: PORTAL }) as R<{ token: string }>
    if (res.data?.token) {
      setToken(res.data.token)
      email.value = emailAddr
      localStorage.setItem(EMAIL_KEY, emailAddr)
    }
    return res
  }

  return { token, email, isLoggedIn, setToken, logout, sendCode, login }
})

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import client from '@/api/client'
import type { R } from '@/types'

const TOKEN_KEY = 'merchant_token'
const PORTAL = 'merchant'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')

  const isLoggedIn = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem(TOKEN_KEY, t)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem(TOKEN_KEY)
  }

  async function sendCode(email: string) {
    return client.post('/auth/email-code', { email, portal: PORTAL }) as Promise<R<unknown>>
  }

  async function login(email: string, code: string) {
    const res = await client.post('/auth/login', { email, code, portal: PORTAL }) as R<{ token: string }>
    if (res.data?.token) setToken(res.data.token)
    return res
  }

  return { token, isLoggedIn, setToken, logout, sendCode, login }
})

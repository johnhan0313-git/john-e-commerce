import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { LoginVO, R } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const isLoggedIn = computed(() => !!token.value)

  async function login(phone: string, password: string) {
    const res = await client.post('/auth/login', { phone, password }) as R<LoginVO>
    token.value = res.data.token
    localStorage.setItem('admin_token', token.value)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem('admin_token')
  }

  return { token, isLoggedIn, login, logout }
})

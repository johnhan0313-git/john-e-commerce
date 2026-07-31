import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('starts logged out', () => {
    const auth = useAuthStore()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.token).toBe('')
  })

  it('setToken and logout', () => {
    const auth = useAuthStore()
    auth.setToken('abc')
    expect(auth.isLoggedIn).toBe(true)
    expect(localStorage.getItem('merchant_token')).toBe('abc')
    auth.logout()
    expect(auth.isLoggedIn).toBe(false)
    expect(localStorage.getItem('merchant_token')).toBeNull()
  })
})

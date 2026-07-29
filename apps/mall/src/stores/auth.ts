import { reactive } from 'vue'
import client from '../api/client'

export const authStore = reactive({
  token: localStorage.getItem('token') || '',
  get isLoggedIn() { return !!this.token },

  async login(username: string, password: string) {
    const res = await client.post('/auth/login', { username, password })
    this.token = res.data.data.token
    localStorage.setItem('token', this.token)
  },

  logout() {
    this.token = ''
    localStorage.removeItem('token')
  },
})

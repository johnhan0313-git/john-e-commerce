import axios from 'axios'
import { ElMessage } from 'element-plus'

const tenantId = import.meta.env.VITE_TENANT_ID || '1'

const client = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Tenant-Id'] = tenantId
  return config
})

client.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(Object.assign(new Error(message), { response: res, businessCode: body.code }))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    const message = err.response?.data?.message || err.message || '请求失败'
    if (status === 401) {
      localStorage.removeItem('admin_token')
      if (!location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    } else if (!err.businessCode) {
      if (status !== 403) {
        ElMessage.error(message)
      } else {
        ElMessage.warning(message || '模块未开通或无权限')
      }
    }
    return Promise.reject(err)
  }
)

export default client

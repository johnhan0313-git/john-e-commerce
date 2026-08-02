import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/base.css'
import { useBrandingStore } from '@/stores/branding'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
useBrandingStore(pinia).fetch()
app.mount('#app')

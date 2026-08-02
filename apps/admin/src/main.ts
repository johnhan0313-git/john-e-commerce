import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import './styles/tokens.css'
import './styles/base.css'
import './styles/element-override.css'
import App from './App.vue'
import router from './router'
import { useBrandingStore } from '@/stores/branding'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
useBrandingStore(pinia).fetch()
app.mount('#app')

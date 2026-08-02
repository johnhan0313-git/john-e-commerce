<template>
  <div class="login-page">
    <aside class="brand-panel">
      <div class="brand-panel-inner">
        <div class="logo-row">
          <img v-if="branding.logo" :src="branding.logo" alt="" class="logo-img" />
          <span v-else class="mark">{{ branding.markLetter }}</span>
          <span class="name">{{ branding.displayName }}</span>
        </div>
        <h1>租户运营<br />一站掌控</h1>
        <p>卖家审核、商品订单、模块开通 —— 多业态电商后台</p>
        <ul class="features">
          <li>多租户模块开关</li>
          <li>卖家入驻与店铺管理</li>
          <li>订单与商品全域视图</li>
        </ul>
      </div>
      <div class="grid-deco" aria-hidden="true" />
    </aside>

    <main class="form-panel">
      <div class="form-box">
        <h2>管理后台登录</h2>
        <p class="sub">使用已开通的管理员邮箱 + 验证码登录（无需密码）</p>
        <el-form label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="邮箱">
            <el-input v-model="email" placeholder="johnhan0313@gmail.com" size="large" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="code-row">
              <el-input v-model="code" placeholder="6 位验证码" maxlength="6" size="large" />
              <el-button size="large" :disabled="cooldown > 0 || sending || !email.trim()" @click="sendCode">
                {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-alert v-if="err" :title="err" type="error" show-icon :closable="false" class="mb" />
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit">
            登录
          </el-button>
          <p class="hint">请先获取邮箱验证码；本地开发可用固定码 123456</p>
        </el-form>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useBrandingStore } from '@/stores/branding'
import { useModulesStore } from '@/stores/modules'

const email = ref('johnhan0313@gmail.com')
const code = ref('')
const err = ref('')
const loading = ref(false)
const sending = ref(false)
const cooldown = ref(0)
let timer: number | undefined

const auth = useAuthStore()
const branding = useBrandingStore()
const modules = useModulesStore()
const router = useRouter()

if (!branding.loaded) branding.fetch()

async function sendCode() {
  err.value = ''
  sending.value = true
  try {
    await auth.sendEmailCode(email.value.trim())
    ElMessage.success('验证码已发送')
    cooldown.value = 60
    timer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0 && timer) {
        clearInterval(timer)
        timer = undefined
      }
    }, 1000)
  } catch (e: any) {
    err.value = e.response?.data?.message || '发送失败'
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  err.value = ''
  loading.value = true
  try {
    await auth.login(email.value.trim(), code.value.trim())
    await modules.fetch()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    err.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(280px, 1.05fr) minmax(320px, 1fr);
  background: var(--color-bg);
}

.brand-panel {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(14, 165, 233, 0.35), transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(56, 189, 248, 0.2), transparent 45%),
    linear-gradient(160deg, #0f172a 0%, #1e293b 55%, #0c4a6e 100%);
  color: #e2e8f0;
  padding: 48px;
  display: flex;
  align-items: center;
}

.brand-panel-inner {
  position: relative;
  z-index: 1;
  max-width: 420px;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 36px;
}

.mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 700;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: contain;
  background: #fff;
}

.name {
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.02em;
}

.brand-panel h1 {
  margin: 0 0 16px;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.03em;
  color: #f8fafc;
}

.brand-panel p {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.6;
  color: #94a3b8;
}

.features {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.features li {
  font-size: 13px;
  color: #cbd5e1;
  padding-left: 18px;
  position: relative;
}

.features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 7px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2);
}

.grid-deco {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse at center, black 20%, transparent 75%);
  pointer-events: none;
}

.form-panel {
  display: grid;
  place-items: center;
  padding: 40px 24px;
}

.form-box {
  width: min(100%, 400px);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 36px 32px;
  box-shadow: var(--shadow);
}

.form-box h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.sub {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--color-muted);
  line-height: 1.5;
}

.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.mb {
  margin-bottom: 16px;
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin: 14px 0 0;
  font-size: 12px;
  color: var(--color-muted);
  line-height: 1.5;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 32px 24px;
    min-height: 220px;
  }

  .brand-panel h1 {
    font-size: 26px;
  }

  .features {
    display: none;
  }
}
</style>
